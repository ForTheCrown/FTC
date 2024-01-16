package net.forthecrown.leaderboards;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Preconditions;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.PluginUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Getter @Setter
public class BoardImpl implements Leaderboard {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final NamespacedKey LEADERBOARD_KEY
      = new NamespacedKey("forthecrown", "leaderboard");

  static final Comparator<LeaderboardScore> ASCENDING_COMPARATOR
      = Comparator.comparingInt(LeaderboardScore::value);

  static final Comparator<LeaderboardScore> DESCENDING_COMPARATOR
      = ASCENDING_COMPARATOR.reversed();

  static final Component DEFAULT_FORMAT = text("${index}) ${entry}: ${score}");

  static final TextReplacementConfig NEW_LINE_REPLACER = TextReplacementConfig.builder()
      .match("\\\\[nN]")
      .replacement("\n")
      .build();

  static final Set<ChatParseFlag> TEXT_FIELD_FLAGS = Set.of(
      ChatParseFlag.IGNORE_CASE,
      ChatParseFlag.IGNORE_SWEARS,
      ChatParseFlag.COLORS,
      ChatParseFlag.EMOJIS,
      ChatParseFlag.GRADIENTS,
      ChatParseFlag.TAGGING,
      ChatParseFlag.TIMESTAMPS
  );

  static final PlayerMessage DEFAULT_YOU = PlayerMessage.allFlags("You");

  String name;

  Holder<LeaderboardSource> source;
  Location location;

  PlayerMessage footer;
  PlayerMessage header;
  PlayerMessage format;
  PlayerMessage youFormat;

  Order order = Order.DESCENDING;
  ScoreFilter filter;

  int maxEntries = DEFAULT_MAX_SIZE;
  boolean fillMissingSlots = false;
  boolean spawned = false;
  boolean includeYou = true;

  TextDisplayMeta displayMeta = new TextDisplayMeta();

  Reference<TextDisplay> ref;

  ServiceImpl service;

  public BoardImpl(String name) {
    this.name = name;
  }

  public static PlayerMessage makeTextFieldMessage(String text) {
    return new PlayerMessage(text, TEXT_FIELD_FLAGS);
  }

  public void setName(String name) {
    Objects.requireNonNull(name, "Null name");
    Preconditions.checkState(service == null,
        "Leaderboard name cannot be modified after being added to manager"
    );

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

    if (service != null) {
      service.getTriggers().onLocationSet(this, location);
    }

    this.location = Locations.clone(location);

    if (this.location != null) {
      this.location.getChunk().addPluginChunkTicket(PluginUtil.getPlugin());

      if (spawned) {
        spawn();

        if (service != null) {
          service.getTriggers().onUpdate(this);
        }
      }
    }
  }

  public Location getLocation() {
    return Locations.clone(location);
  }

  public Component displayName() {
    return text("[" + name + "]", isSpawned() ? NamedTextColor.GREEN : NamedTextColor.GRAY)
        .hoverEvent(infoText());
  }

  public Component infoText() {
    TextWriter writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writeHover(writer);
    return writer.asComponent();
  }

  public void writeHover(TextWriter writer) {
    writer.field("State",
        isSpawned()
            ? text("spawned", NamedTextColor.GREEN)
            : text("inactive", NamedTextColor.GRAY)
    );

    writer.newLine();
    writer.newLine();

    if (source != null) {
      writer.field("Source", source.getKey());
    }

    if (location != null) {
      writer.formattedField("Location", "{0, location, -c -w}", location);
      writer.newLine();
      writer.newLine();
    }

    if (header != null) {
      writer.field("Header", editableTextFormat("header", header));
    }
    if (format != null) {
      writer.field("Format", editableTextFormat("format", format));
    }
    if (footer != null) {
      writer.field("Footer", editableTextFormat("footer", footer));
    }
    if (youFormat != null) {
      writer.field("You-format", editableTextFormat("you-format", youFormat));
    }

    writer.field("Include-you", includeYou);
    writer.field("Max Entries", maxEntries);
    writer.field("Fill empty slots", fillMissingSlots);

    writer.field("Order", Text.prettyEnumName(order));

    if (filter != null) {
      writer.field("Filter", filter);
    }
  }

  private Component editableTextFormat(String argName, PlayerMessage message) {
    return text()
        .append(text("[", NamedTextColor.AQUA))
        .append(text(message.getMessage()))
        .append(text("]", NamedTextColor.AQUA))

        .hoverEvent(text("Click to edit"))

        .clickEvent(ClickEvent.suggestCommand(
            argName.contains("you-format")
                ? String.format("/lb %s set %s %s", argName, name, message.getMessage())
                : String.format("/lb %s %s %s", argName, name, message.getMessage())
        ))

        .build();
  }

  public void setOrder(@NotNull Order order) {
    Objects.requireNonNull(order, "Null order");
    this.order = order;
  }

  @Override
  public boolean fillMissingSlots() {
    return fillMissingSlots;
  }

  public void copyFrom(BoardImpl board) {
    if (board.header != null) {
      this.header = board.header;
    }
    if (board.footer != null) {
      this.footer = board.footer;
    }
    if (board.format != null) {
      this.format = board.format;
    }
    if (board.youFormat != null) {
      this.youFormat = board.youFormat;
    }

    if (board.maxEntries != DEFAULT_MAX_SIZE) {
      this.maxEntries = board.maxEntries;
    }

    this.fillMissingSlots = board.fillMissingSlots;
    this.includeYou = board.includeYou;

    this.displayMeta.copyFrom(board.displayMeta);
  }

  @Override
  public boolean update() {
    if (service == null) {
      return false;
    }

    if (!spawned) {
      return kill();
    }

    if (spawn()) {
      return true;
    }

    var opt = getDisplay();
    if (opt.isEmpty()) {
      return false;
    }

    var display = opt.get();
    applyTo(display);

    service.getTriggers().onUpdate(this);
    return true;
  }

  @Override
  public boolean kill() {
    spawned = false;

    var opt = getDisplay();
    if (opt.isEmpty()) {
      return false;
    }
    opt.get().remove();
    return true;
  }

  @Override
  public boolean spawn() {
    if (location == null || getDisplay().isPresent()) {
      return false;
    }

    var textDisplay = location.getWorld().spawn(location, TextDisplay.class);
    applyTo(textDisplay);

    ref = new WeakReference<>(textDisplay);
    spawned = true;

    if (service != null) {
      service.getTriggers().onUpdate(this);
    }

    return true;
  }

  private void applyTo(TextDisplay display) {
    display.addScoreboardTag(getName());
    display.getPersistentDataContainer().set(LEADERBOARD_KEY, PersistentDataType.STRING, name);
    displayMeta.apply(display);
  }

  private Component renderPlaceholders(Component component, Audience viewer) {
    PlaceholderRenderer renderer = Placeholders.newRenderer().useDefaults();
    return renderer.render(component.replaceText(NEW_LINE_REPLACER), viewer);
  }

  public Component renderText(@Nullable Audience viewer) {
    var builder = text();
    if (header != null) {
      builder.append(renderPlaceholders(header.create(viewer), viewer));
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

        Component line = renderLine(viewer, i, null, null, null);
        builder.appendNewline().append(line);

        continue;
      }

      LeaderboardScore score = entries.get(i);
      UUID playerId = score.playerId();
      int value = score.value();

      if (viewingUser != null && !viewerWasShown) {
        viewerWasShown = Objects.equals(viewingUser.getUniqueId(), playerId);
      }

      Component line = renderLine(viewer, i, score.displayName(viewer), value, playerId);
      builder.appendNewline().append(line);
    }

    int vIndex = findViewerIndex(viewingUser, entries);
    if (!viewerWasShown && vIndex != -1 && viewingUser != null && includeYou) {
      source.getValue().getScore(viewingUser.getUniqueId())
          .ifPresent(value -> {
            if (filter != null && !filter.test(value)) {
              return;
            }

            Component line = renderLine(
                viewer,
                vIndex,
                text("You"),
                value,
                viewingUser.getUniqueId()
            );

            builder.appendNewline().append(line);
          });
    }

    if (footer != null) {
      builder.appendNewline().append(renderPlaceholders(footer.create(viewer), viewer));
    }

    return builder.build();
  }

  private int findViewerIndex(User user, List<LeaderboardScore> scores) {
    if (user == null) {
      return -1;
    }

    for (int i = 0; i < scores.size(); i++) {
      var score = scores.get(i);

      if (Objects.equals(user.getUniqueId(), score.playerId())) {
        return i;
      }
    }

    return -1;
  }

  private Component renderLine(
      Audience viewer,
      int index,
      Component displayName,
      Integer value,
      UUID playerId
  ) {
    String timerScore = value == null ? "-" : getTimerCounter(value);

    PlaceholderRenderer renderer = Placeholders.newRenderer();
    renderer.useDefaults();
    renderer.add("index", index+1);
    renderer.add("entry", displayName == null ? text("-") : displayName);
    renderer.add("score", value == null ? text("-") : Text.formatNumber(value));
    renderer.add("score.raw", value);
    renderer.add("score.timer", timerScore);

    Map<String, Object> ctx = new HashMap<>();
    ctx.put("index", index);
    ctx.put("score", value);
    ctx.put("timerScore", timerScore);
    ctx.put("playerId", playerId);

    Component format;

    if (displayName != null && Text.plain(displayName).equals("You") && youFormat != null) {
      format = youFormat.create(viewer);
    } else if (this.format == null) {
      format = DEFAULT_FORMAT;
    } else {
      format = this.format.create(viewer);
    }

    return renderer.render(format.replaceText(NEW_LINE_REPLACER), viewer, ctx);
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

  Optional<TextDisplay> getDisplay() {
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

    Chunk chunk = location.getChunk();

    if (!chunk.isEntitiesLoaded()) {
      // Force-load entities
      Entity[] entityArray = chunk.getEntities();
      TextDisplay fromArray = findFromArray(entityArray);

      if (fromArray != null) {
        ref = new WeakReference<>(fromArray);
        return Optional.of(fromArray);
      }
    }

    Collection<TextDisplay> nearby = location.getNearbyEntitiesByType(TextDisplay.class, .5);
    nearby.removeIf(textDisplay -> !isBoardEntity(textDisplay));

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

  private boolean isBoardEntity(Entity entity) {
    if (!(entity instanceof TextDisplay)) {
      return false;
    }

    var pdc = entity.getPersistentDataContainer();
    if (!pdc.has(LEADERBOARD_KEY)) {
      return false;
    }

    String string = pdc.get(LEADERBOARD_KEY, PersistentDataType.STRING);
    return Objects.equals(string, name);
  }

  private TextDisplay findFromArray(Entity[] entities) {
    for (Entity entity : entities) {
      if (isBoardEntity(entity)) {
        return (TextDisplay) entity;
      }
    }

    return null;
  }

  private List<LeaderboardScore> getSortedScores() {
    if (source == null) {
      return List.of();
    }

    List<LeaderboardScore> list = new ArrayList<>(source.getValue().getScores());

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
