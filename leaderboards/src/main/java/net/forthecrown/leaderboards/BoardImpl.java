package net.forthecrown.leaderboards;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.PluginUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class BoardImpl implements Leaderboard {

  public static final NamespacedKey LEADERBOARD_KEY
      = new NamespacedKey("forthecrown", "leaderboard");

  static final Comparator<LeaderboardScore> ASCENDING_COMPARATOR
      = Comparator.comparingInt(LeaderboardScore::value);

  static final Comparator<LeaderboardScore> DESCENDING_COMPARATOR
      = ASCENDING_COMPARATOR.reversed();

  final String name;

  LeaderboardSource source;
  Location location;

  Component footer;
  Component header;
  Component format;

  Order order = Order.DESCENDING;
  ScoreFilter filter;

  int maxEntries = DEFAULT_MAX_SIZE;
  boolean fillMissingSlots = false;

  Reference<TextDisplay> ref;

  public BoardImpl(String name) {
    this.name = name;
  }

  public void setLocation(Location location) {
    boolean spawned = isSpawned();

    if (spawned) {
      kill();
    }

    if (this.location != null) {
      this.location.getChunk().removePluginChunkTicket(PluginUtil.getPlugin());
    }

    this.location = Locations.clone(location);

    if (this.location != null) {
      this.location.getChunk().addPluginChunkTicket(PluginUtil.getPlugin());

      if (spawned) {
        spawn();
      }
    }
  }

  public Location getLocation() {
    return Locations.clone(location);
  }

  public void setOrder(@NotNull Order order) {
    Objects.requireNonNull(order, "Null order");
    this.order = order;
  }

  @Override
  public boolean fillMissingSlots() {
    return fillMissingSlots;
  }

  @Override
  public boolean update() {
    if (spawn()) {
      return true;
    }

    var opt = getDisplay();
    if (opt.isEmpty()) {
      return false;
    }

    var display = opt.get();
    applyTo(display);

    return true;
  }

  @Override
  public boolean kill() {
    var opt = getDisplay();
    if (opt.isEmpty()) {
      return false;
    }
    opt.get().remove();
    return true;
  }

  @Override
  public boolean spawn() {
    if (isSpawned() || location == null) {
      return false;
    }

    var textDisplay = location.getWorld().spawn(location, TextDisplay.class);
    applyTo(textDisplay);

    ref = new WeakReference<>(textDisplay);

    return true;
  }

  private void applyTo(TextDisplay display) {
    display.addScoreboardTag(getName());

    var text = renderText(null);
    display.text(text);
  }

  private Component renderPlaceholders(Component component, Audience viewer) {
    PlaceholderRenderer renderer = Placeholders.newRenderer().useDefaults();
    return renderer.render(component, viewer);
  }

  public Component renderText(@Nullable Audience viewer) {
    var builder = Component.text();
    if (header != null) {
      builder.append(renderPlaceholders(header, viewer));
    }

    List<LeaderboardScore> entries = getSortedScores();
    int end = fillMissingSlots ? this.maxEntries : Math.min(this.maxEntries, entries.size());

    if (end == -1) {
      end = entries.size();
    }

    boolean viewerWasShown = false;
    User viewingUser = Audiences.getUser(viewer);

    for (int i = 0; i < end; i++) {
      if (i >= entries.size()) {
        PlaceholderRenderer renderer = Placeholders.newRenderer();
        renderer.useDefaults();
        renderer.add("index", i+1);
        renderer.add("entry", "-");
        renderer.add("score", "-");
        renderer.add("score.raw", "-");
        renderer.add("score.timer", "-");

        Component line = renderer.render(format, viewer);
        builder.appendNewline().append(line);

        continue;
      }

      LeaderboardScore score = entries.get(i);
      UUID playerId = score.playerId();
      int value = score.value();

      String timerScore = getTimerCounter(value);

      PlaceholderRenderer renderer = Placeholders.newRenderer();
      renderer.useDefaults();
      renderer.add("index", i+1);
      renderer.add("entry", (match, render) -> score.displayName(render.viewer()));
      renderer.add("score", Text.formatNumber(value));
      renderer.add("score.raw", value);
      renderer.add("score.timer", timerScore);

      if (viewingUser != null && !viewerWasShown) {
        viewerWasShown = Objects.equals(viewingUser.getUniqueId(), playerId);
      }

      Map<String, Object> ctx = new HashMap<>();
      ctx.put("index", i);
      ctx.put("score", value);
      ctx.put("timerScore", timerScore);
      ctx.put("entry", score);
      ctx.put("playerId", playerId);

      Component line = renderer.render(format, viewer, ctx);
      builder.appendNewline().append(line);
    }

    if (footer != null) {
      builder.appendNewline().append(renderPlaceholders(footer, viewer));
    }

    return builder.build();
  }

  public static String getTimerCounter(long millis) {
    long minutes      = (millis / 60000);
    long seconds      = (millis /  1000) %  60;
    long milliseconds = (millis /    10) % 100;

    String prefix = "";

    if (minutes >= 60) {
      double hours = Math.floor((double) minutes / 60);
      prefix = String.format("%02.0f:", hours);
      minutes = minutes % 60;
    }

    return prefix + String.format("%02d:%02d.%02d", minutes, seconds, milliseconds);
  }

  @Override
  public boolean isSpawned() {
    return getDisplay().isPresent();
  }

  private Optional<TextDisplay> getDisplay() {
    if (ref == null) {
      return locateDisplay();
    }

    return Optional.ofNullable(ref.get())
        .filter(Entity::isValid)
        .or(this::locateDisplay);
  }

  private Optional<TextDisplay> locateDisplay() {
    if (location == null) {
      return Optional.empty();
    }

    var chunk = location.getChunk();

    if (!chunk.isEntitiesLoaded()) {
      // Force-load entities
      var entityArray = chunk.getEntities();
    }

    var nearby = location.getNearbyEntitiesByType(TextDisplay.class, .5);
    nearby.removeIf(textDisplay -> !textDisplay.getPersistentDataContainer().has(LEADERBOARD_KEY));

    if (nearby.isEmpty()) {
      return Optional.empty();
    }

    TextDisplay display;

    if (nearby.size() > 1) {
      var it = nearby.iterator();
      display = it.next();

      while (it.hasNext()) {
        var n = it.next();
        n.remove();
      }
    } else {
      display = nearby.iterator().next();
    }

    ref = new WeakReference<>(display);

    return Optional.of(display);
  }

  private List<LeaderboardScore> getSortedScores() {
    if (source == null) {
      return List.of();
    }

    List<LeaderboardScore> list = new ArrayList<>(source.getScores());

    Comparator<LeaderboardScore> comparator = getComparator();
    list.sort(comparator);

    if (filter != null) {
      list.removeIf(leaderboardScore -> {
        int v = leaderboardScore.value();
        return !filter.test(v);
      });
    }

    return list;
  }

  Comparator<LeaderboardScore> getComparator() {
    return switch (order) {
      case ASCENDING -> ASCENDING_COMPARATOR;
      case DESCENDING -> DESCENDING_COMPARATOR;
    };
  }
}
