package net.forthecrown.challenges;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.longs.LongList;
import java.time.LocalDate;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.utils.DateRange;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;

@RequiredArgsConstructor
public class Streak {

  public static final String
      KEY_TIMES   = "completionTimes",
      KEY_HIGHEST = "highestEver";

  public static final int NOT_COMPUTED = -1;
  public static final int NO_STREAK = 0;

  @Getter
  private final StreakCategory category;

  @Getter(AccessLevel.PACKAGE)
  private final LongList completionTimes = new LongArrayList();

  @Getter @Setter
  private int highest;

  private int cachedStreak = NOT_COMPUTED;

  public int increase(long time) {
    completionTimes.add(time);
    sort();

    this.cachedStreak = NOT_COMPUTED;
    return get();
  }

  /** Recalculate the streak value */
  public void reset() {
    cachedStreak = NOT_COMPUTED;

    // get() to force cachedStreak to be recalculated
    get();
  }

  private void sort() {
    completionTimes.sort(LongComparators.NATURAL_COMPARATOR);
  }

  /**
   * Gets the streak value.
   * <p>
   * Under most circumstances, the cached streak value is returned, but if no
   * valid value exists, it will be calculated
   *
   * @return Streak value
   */
  public int get() {
    if (completionTimes.isEmpty()) {
      return NO_STREAK;
    }

    if (cachedStreak != NOT_COMPUTED) {
      return cachedStreak;
    }

    int streak = 0;
    DateRange range = category.searchRange(LocalDate.now());

    boolean optional = true;
    int index = completionTimes.size() - 1;

    while (index >= 0) {
      long time = completionTimes.getLong(index--);
      LocalDate localDate = Time.localDate(time);
      long epoch = localDate.toEpochDay();

      if (epoch > range.getMaxEpoch()) {
        continue;
      }

      if (epoch >= range.getMinEpoch()) {
        ++streak;
        range = category.moveRange(range);
      } else if (!optional) {
        break;
      } else {
        // Retry this index again, but with
        // Altered range, since this is still optional
        index++;
        range = category.moveRange(range);
      }

      optional = false;
    }

    if (streak == NO_STREAK) {
      cachedStreak = NOT_COMPUTED;
    } else {
      cachedStreak = streak;
    }

    this.highest = Math.max(streak, this.highest);
    return streak;
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public JsonObject serialize() {
    JsonObject obj = new JsonObject();

    if (highest > 0) {
      obj.addProperty(KEY_HIGHEST, highest);
    }

    var arr = JsonUtils.ofStream(
        completionTimes.longStream()
            .mapToObj(Date::new)
            .map(JsonUtils::writeDate)
    );


    if (!arr.isEmpty()) {
      obj.add(KEY_TIMES, arr);
    }

    return obj;
  }

  public void deserialize(JsonObject obj) {
    JsonWrapper json = JsonWrapper.wrap(obj);
    this.highest = json.getInt(KEY_HIGHEST, 0);
    completionTimes.clear();

    if (obj.has(KEY_TIMES)) {
      JsonUtils.stream(json.getArray(KEY_TIMES))
          .mapToLong(JsonUtils::readTimestamp)
          .forEach(completionTimes::add);

      sort();

      // get() to cache streak and max out highest
      cachedStreak = NOT_COMPUTED;
      get();
    }
  }
}