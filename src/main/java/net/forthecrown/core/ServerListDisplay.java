package net.forthecrown.core;

import static net.forthecrown.utils.MonthDayPeriod.ALL;
import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.script2.Script;
import net.forthecrown.utils.MonthDayPeriod;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;

/**
 * Dynamically/randomly changes the server icon
 */
@Getter
public class ServerListDisplay {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final Comparator<Holder<Entry>> ENTRY_COMPARATOR
      = Holder.comparingByValue();

  public static final Pair<CachedServerIcon, Component> NULL_PAIR
      = Pair.of(null, null);

  @Getter
  private static final ServerListDisplay instance = new ServerListDisplay();

  /** Key of the default display entry */
  public static final String DEFAULT = "default";

  /** Registry of all display entries */
  private final Registry<Entry> registry = Registries.newRegistry();

  /** Cached list of entries are applicable to the current date */
  private final List<Holder<Entry>> dateCache = new ArrayList<>();

  /** Icons directory */
  private final Path directory;

  /** The <code>serverlist.toml</code> file */
  private final Path loaderFile;

  ServerListDisplay() {
    this.directory = PathUtil.getPluginDirectory("icons");
    this.loaderFile = PathUtil.pluginPath("serverlist.toml");

    saveDefaults();
  }

  void saveDefaults() {
    try {
      FtcJar.saveResources(
          "icons",
          directory,
          ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
      );
      FtcJar.saveResources(
          "serverlist.toml",
          loaderFile,
          ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
      );

      LOGGER.debug("Saved default server icon directory");
    } catch (IOException exc) {
      LOGGER.error("Couldn't save default icons!", exc);
    }
  }

  /** Caches all registered entries that are applicable to the current date */
  @OnDayChange
  void cacheDateEntries() {
    dateCache.clear();

    LocalDate today = LocalDate.now();
    registry.entries()
        .stream()
        .filter(holder -> {
          if (holder.getKey().equals(DEFAULT)) {
            return false;
          }

          var val = holder.getValue();

          if (val.getIcons().isEmpty() && val.getMotdPart() == null) {
            return false;
          }

          var period = val.getPeriod();
          return period == null || period.contains(today);
        })
        .forEach(dateCache::add);

    dateCache.sort(ENTRY_COMPARATOR);
  }

  public Pair<CachedServerIcon, Component> getCurrent() {
    if (registry.isEmpty()) {
      return NULL_PAIR;
    }

    var rand = Util.RANDOM;
    var date = LocalDate.now();

    CachedServerIcon icon = null;
    Component motdPart = null;

    for (Holder<Entry> i : dateCache) {
      var val = i.getValue();
      LOGGER.debug("testing icon={}", i.getKey());

      if (!val.shouldUse(date, rand)) {
        continue;
      }

      if (icon == null) {
        icon = val.get(rand);
      }

      if (motdPart == null) {
        motdPart = val.getMotdPart();
      }

      // Both icon and MOTD have been found, stop here
      if (icon != null && motdPart != null) {
        return Pair.of(icon, motdPart);
      }
    }

    // Either MOTD, icon or both are missing, get the default entry
    // and fill any missing data with it
    Optional<Holder<Entry>> def = registry.getHolder(DEFAULT);

    if (def.isEmpty()) {
      return Pair.of(icon, motdPart);
    } else {
      var defaultValue = def.get().getValue();
      return Pair.of(
          icon == null ? defaultValue.get(rand) : icon,
          motdPart == null ? defaultValue.motdPart : motdPart
      );
    }
  }

  @OnLoad
  public void load() {
    registry.clear();

    SerializationHelper.readTomlAsJson(loaderFile, wrapper -> {
      for (var e : wrapper.entrySet()) {
        if (!Registries.isValidKey(e.getKey())) {
          LOGGER.warn("Invalid icon key found! '{}'", e.getKey());
          continue;
        }

        Entry icon;
        JsonElement element = e.getValue();

        if (element.isJsonPrimitive() || element.isJsonArray()) {
          icon = new Entry(null, readIconList(element), null, null, 0);
        } else {
          var json = JsonWrapper.wrap(element.getAsJsonObject());

          List<CachedServerIcon> icons = readIconList(
              json.get("icons")
          );

          MonthDayPeriod period = ALL;
          Script condition = null;
          Component motdPart = null;
          int prio = json.getInt("priority", 0);

          if (json.has("condition")) {
            condition = Script.read(json.get("condition"), true);
            condition.compile();
          }

          if (json.has("period")) {
            period = MonthDayPeriod.load(json.get("period"));
          }

          if (json.has("motd")) {
            motdPart = json.getComponent("motd");
          }

          if (icons.isEmpty() && motdPart == null) {
            LOGGER.warn(
                "Found display entry with no MOTD section and no icons: {}",
                e.getKey()
            );
            continue;
          }

          icon = new Entry(period, icons, condition, motdPart, prio);
        }

        if (icon.icons.isEmpty()) {
          continue;
        }

        registry.register(e.getKey(), icon);
        LOGGER.debug("Loaded serverlist display entry {}", e.getKey());
      }
    });

    cacheDateEntries();
  }

  private List<CachedServerIcon> readIconList(JsonElement element) {
    if (element.isJsonPrimitive()) {
      var stringPath = element.getAsString();
      Path path = directory.resolve(stringPath);
      var icon = loadImage(path);

      if (icon == null) {
        return List.of();
      }

      return List.of(icon);
    }

    var arr = element.getAsJsonArray();
    List<CachedServerIcon> icons = new ObjectArrayList<>(arr.size());

    for (int i = 0; i < arr.size(); i++) {
      var stringPath = arr.get(i).getAsString();
      Path path = directory.resolve(stringPath);
      var icon = loadImage(path);

      if (icon == null) {
        continue;
      }

      icons.add(icon);
    }

    return icons;
  }

  private CachedServerIcon loadImage(Path path) {
    if (Files.notExists(path)) {
      LOGGER.warn("Icon '{}' doesn't exist!", path);
      return null;
    }

    try {
      return Bukkit.loadServerIcon(path.toFile());
    } catch (Exception exc) {
      LOGGER.error("Couldn't load server icon '{}'", path, exc);
      return null;
    }
  }

  @Getter
  @RequiredArgsConstructor
  static class Entry implements Comparable<Entry> {

    private final MonthDayPeriod period;
    private final List<CachedServerIcon> icons;
    private final Script condition;
    private final Component motdPart;
    private final int priority;

    public CachedServerIcon get(Random random) {
      if (icons.isEmpty()) {
        return null;
      }

      if (icons.size() == 1) {
        return icons.get(0);
      }

      return icons.get(random.nextInt(icons.size()));
    }

    public boolean shouldUse(LocalDate date, Random random) {
      if (condition == null) {
        return true;
      }

      condition.put("random", random);
      condition.put("date", date);

      return condition.eval()
          .asBoolean()
          .orElse(false);
    }

    @Override
    public String toString() {
      return "%s[condition=%s, motd=%s, period=%s, iconsSize=%s, priority=%s]".formatted(
          getClass().getSimpleName(),
          getCondition(),
          getMotdPart() == null ? null : Text.plain(getMotdPart()),
          getPeriod(),
          getIcons().size(),
          getPriority()
      );
    }

    @Override
    public int compareTo(@NotNull ServerListDisplay.Entry o) {
      return Integer.compare(o.getPriority(), getPriority());
    }
  }
}