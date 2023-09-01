package net.forthecrown.challenges;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.io.source.Source;
import org.slf4j.Logger;

@Getter
public class ChallengeDataStorage {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path directory;
  private final Path itemDataDirectory;

  private final Path challengesFile;
  private final Path itemChallengesFile;
  private final Path userDataFile;
  private final Path streakScriptFile;
  private final Path resetDataFile;

  public ChallengeDataStorage(Path directory) {
    this.directory = directory;

    // Directories
    this.itemDataDirectory  = this.directory.resolve("item_data");

    // Files
    this.challengesFile     = this.directory.resolve("challenges.toml");
    this.itemChallengesFile = this.directory.resolve("item_challenges.toml");
    this.userDataFile       = this.directory.resolve("user_data.json");
    this.streakScriptFile   = this.directory.resolve("streak_scripts.toml");
    this.resetDataFile      = this.directory.resolve("reset_data.json");
  }

  void ensureDefaultsExist() {
    PluginJar.saveResources("challenges", directory);
  }

  /**
   * Gets all callback scripts for a streak category, these scripts are intended to be called when a
   * user increments their streak progress to give them rewards.
   */
  public Set<Source> getScripts(StreakCategory category) {
    Set<Source> result = new ObjectOpenHashSet<>();

    SerializationHelper.readAsJson(streakScriptFile, json -> {
      var element = json.get(category.name().toLowerCase());

      if (element == null) {
        return;
      }

      // If a single script
      if (element.isJsonPrimitive()) {
        result.add(Scripts.loadScriptSource(element, false));
        return;
      }

      // If an array of scripts is declared
      JsonUtils.stream(element.getAsJsonArray())
          .map(scriptElement -> Scripts.loadScriptSource(scriptElement, false))
          .forEach(result::add);
    });

    return result;
  }

  public void loadChallenges(Registry<Challenge> target) {
    loadChallenges(target, getChallengesFile(), ScriptedChallengeLoader::parse);
  }

  public void loadItemChallenges(Registry<Challenge> registry) {
    loadChallenges(registry, getItemChallengesFile(), ItemChallengeLoader::parse);
  }

  private void loadChallenges(
      Registry<Challenge> registry,
      Path path,
      Function<JsonObject, Result<? extends Challenge>> parser
  ) {
    SerializationHelper.readAsJson(path, json -> {
      int loaded = 0;

      for (var e : json.entrySet()) {
        if (!Registries.isValidKey(e.getKey())) {
          LOGGER.warn("Invalid key found: '{}'", e.getKey());
          continue;
        }

        if (!e.getValue().isJsonObject()) {
          LOGGER.warn("Expected JSON Object, found: {} in '{}'",
              e.getValue().getClass(), e.getKey()
          );

          continue;
        }

        var result = parser.apply(e.getValue().getAsJsonObject())
            .mapError(s -> e.getKey() + ": " + s);

        if (result.isError()) {
          LOGGER.error(result.getError());
          continue;
        }

        ++loaded;
        registry.register(e.getKey(), result.getValue());
      }

      LOGGER.debug("Loaded {} challenges from {}", loaded, path);
    });
  }

  /* ----------------------------- USER DATA ------------------------------ */

  public DataResult<List<ChallengeEntry>> loadEntries() {
    return SerializationHelper.readJson(getUserDataFile())
        .map(object -> {
          List<ChallengeEntry> entries = new ObjectArrayList<>();

          for (var e : object.entrySet()) {
            if (!e.getValue().isJsonObject()) {
              LOGGER.warn(
                  "Found non object in entry json file under {}",
                  e.getKey()
              );

              continue;
            }

            UUID uuid = UUID.fromString(e.getKey());
            ChallengeEntry entry = new ChallengeEntry(uuid);

            var eObj = JsonWrapper.wrap(e.getValue().getAsJsonObject());
            entry.deserialize(eObj);

            entries.add(entry);
          }

          LOGGER.debug("Loaded {} challenge entries", entries.size());
          return entries;
        });
  }

  public void saveEntries(Collection<ChallengeEntry> entries) {
    SerializationHelper.writeJsonFile(getUserDataFile(), wrapper -> {
      for (var e : entries) {
        JsonWrapper json = JsonWrapper.create();
        e.serialize(json);

        if (json.isEmpty()) {
          continue;
        }

        wrapper.add(e.getId().toString(), json);
      }

      LOGGER.debug("Saved {} challenge entries", wrapper.size());
    });
  }

  /* ----------------------------- ITEM DATA ------------------------------ */

  public Path getItemFile(String holder) {
    return itemDataDirectory.resolve(holder + ".dat");
  }

  public ChallengeItemContainer loadContainer(Holder<Challenge> holder) {
    ChallengeItemContainer
        container = new ChallengeItemContainer(holder.getKey());

    SerializationHelper.readTagFile(
        getItemFile(container.getChallengeKey()),
        container::load
    );

    return container;
  }

  public void saveContainer(ChallengeItemContainer container) {
    if (container.isEmpty()) {
      PathUtil.safeDelete(getItemFile(container.getChallengeKey()));
      return;
    }

    SerializationHelper.writeTagFile(
        getItemFile(container.getChallengeKey()),
        container::save
    );
  }

  /* ------------------------------ ACTIVE -------------------------------- */

  public void saveActive(Collection<Holder<Challenge>> holders,
                         Map<ResetInterval, Long> lastResets
  ) {
    SerializationHelper.writeJsonFile(getResetDataFile(), wrapper -> {
      EnumMap<ResetInterval, JsonArray> savedMap
          = new EnumMap<>(ResetInterval.class);

      for (var h: holders) {
        var c = h.getValue();
        var arr = savedMap.computeIfAbsent(
            c.getResetInterval(),
            interval -> new JsonArray()
        );

        arr.add(h.getKey());
      }

      savedMap.forEach((interval, array) -> {
        JsonWrapper json = JsonWrapper.create();
        long lastReset = lastResets.getOrDefault(interval, 0L);

        if (lastReset != 0) {
          json.addTimeStamp("lastReset", lastReset);
        }

        json.add("values", array);
        wrapper.add(interval.name().toLowerCase(), json);
      });
    });
  }

  public void loadActive(Collection<Holder<Challenge>> holders,
                         Map<ResetInterval, Long> lastResets,
                         Registry<Challenge> registry
  ) {
    holders.clear();
    lastResets.clear();

    SerializationHelper.readJsonFile(getResetDataFile(), wrapper -> {
      wrapper.entrySet().forEach(e -> {
        ResetInterval interval = ResetInterval.valueOf(e.getKey().toUpperCase());
        JsonWrapper json = JsonWrapper.wrap(e.getValue().getAsJsonObject());

        lastResets.put(interval, json.getTimeStamp("lastReset"));

        JsonArray arr = json.getArray("values");
        JsonUtils.stream(arr)
            .map(JsonElement::getAsString)
            .flatMap(s -> {
              var opt = registry.getHolder(s);

              if (opt.isEmpty()) {
                LOGGER.warn("Unknown challenge {} found in active.json", s);
                return Stream.empty();
              }

              return Stream.of(opt.get());
            })

            .forEach(holders::add);
      });
    });
  }
}