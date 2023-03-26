package net.forthecrown.core.challenge;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
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
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

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
    this.itemDataDirectory = directory.resolve("item_data");

    // Files
    this.challengesFile = directory.resolve("challenges.toml");
    this.itemChallengesFile = directory.resolve("item_challenges.toml");
    this.userDataFile = directory.resolve("user_data.json");
    this.streakScriptFile = directory.resolve("streak_scripts.toml");
    this.resetDataFile = directory.resolve("reset_data.json");
  }

  void ensureDefaultsExist() {
    try {
      FtcJar.saveResources(
          "challenges",
          ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
      );

      // Default scripts are saved by ScriptManager
    } catch (IOException exc) {
      LOGGER.error("Error trying to save challenge defaults!", exc);
    }
  }

  /**
   * Gets all callback scripts for a streak category, these scripts are intended to be called when a
   * user increments their streak progress to give them rewards.
   */
  public Set<String> getScripts(StreakCategory category) {
    Set<String> result = new ObjectOpenHashSet<>();

    SerializationHelper.readTomlAsJson(streakScriptFile, json -> {
      var element = json.get(category.name().toLowerCase());

      if (element == null) {
        return;
      }

      // If a single script
      if (element.isJsonPrimitive()) {
        result.add(element.getAsString());
        return;
      }

      // If an array of scripts is declared
      JsonUtils.stream(element.getAsJsonArray())
          .map(JsonElement::getAsString)
          .forEach(result::add);
    });

    return result;
  }

  public void loadChallenges(Registry<Challenge> target) {
    _loadChallenges(
        target,
        getChallengesFile(),
        ChallengeParser::parse
    );
  }

  public void loadItemChallenges(Registry<Challenge> registry) {
    _loadChallenges(
        registry,
        getItemChallengesFile(),
        ItemChallengeParser::parse
    );
  }

  private void _loadChallenges(Registry<Challenge> registry,
                               Path path,
                               Function<JsonObject, DataResult<? extends Challenge>> parser
  ) {
    SerializationHelper.readTomlAsJson(path, json -> {
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

        ++loaded;
        registry.register(
            e.getKey(),

            parser.apply(e.getValue().getAsJsonObject())
                .mapError(s -> e.getKey() + ": " + s)
                .getOrThrow(true, s -> {
                })
        );
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