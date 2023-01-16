package net.forthecrown.datafix;

import static net.forthecrown.core.challenge.ChallengeEntry.KEY_COMPLETED;
import static net.forthecrown.core.challenge.ChallengeEntry.KEY_PROGRESS;
import static net.forthecrown.core.challenge.ChallengeEntry.KEY_STREAKS;
import static net.forthecrown.log.DateRange.MILLIS_PER_DAY;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.challenge.ChallengeDataStorage;
import net.forthecrown.core.challenge.ChallengeLogs;
import net.forthecrown.core.challenge.ResetInterval;
import net.forthecrown.core.challenge.Streak;
import net.forthecrown.core.challenge.StreakCategory;
import net.forthecrown.log.LogManager;
import net.forthecrown.log.LogQuery;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;

@RequiredArgsConstructor
public class ChallengesLogFix extends DataUpdater {
  public static final String LEGACY_STREAK_KEY = "highest::streak";

  private final ChallengeDataStorage storage;

  @Override
  protected boolean update() throws Throwable {
    fixEntries();

    LOGGER.info("Entries fixed");

    var logs = LogManager.getInstance();
    long dayStart = LocalDate.now().toEpochDay() * MILLIS_PER_DAY;

    var active = logs.queryLogs(
        LogQuery.builder(ChallengeLogs.ACTIVE)
            .queryRange(StreakCategory.WEEKLY.searchRange(LocalDate.now()))
            .build()
    );

    LOGGER.info("active before filter={}", active);

    active.removeIf(entry -> {
      var type = entry.get(ChallengeLogs.A_TYPE);

      if (type != ResetInterval.DAILY) {
        return false;
      }

      long time = entry.getDate();
      return time < dayStart;
    });

    LOGGER.info("active after filter={}", active);

    EnumMap<ResetInterval, JsonArray> data
        = new EnumMap<>(ResetInterval.class);

    active.forEach(entry -> {
      JsonArray arr = data.computeIfAbsent(
          entry.get(ChallengeLogs.A_TYPE),
          interval -> new JsonArray()
      );

      arr.add(entry.get(ChallengeLogs.A_CHALLENGE));
    });

    SerializationHelper.writeJsonFile(storage.getResetDataFile(), wrapper -> {
      data.forEach((interval, array) -> {
        LOGGER.info("Saving reset category={}", interval);
        LOGGER.info("values={}", array);

        JsonWrapper json = JsonWrapper.create();
        json.add("lastReset", -1L);
        json.add("values", array);

        wrapper.add(interval.name().toLowerCase(), json);
      });
    });

    return true;
  }

  private void fixEntries() {
    Path users = storage.getUserDataFile();

    var obj = SerializationHelper.readJson(users)
        .resultOrPartial(LOGGER::error)
        .orElseThrow();

    JsonWrapper resultJson = JsonWrapper.create();
    for (var e: obj.entrySet()) {
      UUID uuid = UUID.fromString(e.getKey());
      JsonWrapper entryResult = JsonWrapper.create();

      try {
        JsonObject j = e.getValue().getAsJsonObject();
        j.remove(LEGACY_STREAK_KEY);

        entryResult.add(KEY_STREAKS, fixStreaks(uuid));
        entryResult.add(KEY_COMPLETED, fixCompleted(uuid));
        entryResult.add(KEY_PROGRESS, j);

        LOGGER.info("Fixed entry {}", uuid);
      } catch (Throwable t) {
        LOGGER.error("Error fixing entry {}", uuid, t);
      }

      resultJson.add(uuid.toString(), entryResult);
    }

    LOGGER.info("Writing {}", users);
    SerializationHelper.writeJson(users, resultJson.getSource());
  }

  private JsonArray fixCompleted(UUID entry) {
    var logs = LogManager.getInstance();
    var query = LogQuery.builder(ChallengeLogs.COMPLETED)
        .queryRange(StreakCategory.WEEKLY.searchRange(LocalDate.now()))

        .field(ChallengeLogs.C_PLAYER)
        .add(Objects::nonNull)
        .add(uuid -> Objects.equals(uuid, entry))

        .build();

    JsonArray array = new JsonArray();
    logs.queryLogs(query).forEach(e -> {
      array.add(e.get(ChallengeLogs.C_CHALLENGE));
    });

    LOGGER.info("{} has completed {}", entry, array);
    return array;
  }

  private JsonWrapper fixStreaks(UUID uuid) {
    var logs = LogManager.getInstance();
    var query = LogQuery.builder(ChallengeLogs.STREAK_SCHEMA)
        .queryRange(logs.getLogRange())

        .field(ChallengeLogs.S_PLAYER)
        .add(Objects::nonNull)
        .add(uuid1 -> Objects.equals(uuid1, uuid))

        .build();

    var streaks = logs.queryLogs(query);

    final EnumMap<StreakCategory, Streak> streakMap
        = new EnumMap<>(StreakCategory.class);

    for (var v: streaks) {
      StreakCategory category = v.get(ChallengeLogs.S_CATEGORY);
      var streak = streakMap.computeIfAbsent(category, Streak::new);

      streak.increase(v.getDate());
    }

    JsonWrapper streakJson = JsonWrapper.create();
    streakMap.forEach((category, streak) -> {
      streakJson.add(
          category.name().toLowerCase(),
          streak.serialize()
      );
    });

    return streakJson;
  }
}