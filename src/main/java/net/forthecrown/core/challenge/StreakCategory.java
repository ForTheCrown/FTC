package net.forthecrown.core.challenge;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.log.DateRange;

/**
 * A category for streaks, each challenge selects its own category.
 * <p>
 * For a player to get a streak in a category, it must complete all challenges in a streak category
 * in an allowed time frame.
 */
@RequiredArgsConstructor
public enum StreakCategory {
  /**
   * Challenges which reset daily
   */
  DAILY("Daily"),

  /**
   * Challenges which reset weekly
   */
  WEEKLY("Weekly") {
    @Override
    public DateRange searchRange(LocalDate date) {
      var start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      var end = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

      return DateRange.between(start, end);
    }

    @Override
    public DateRange moveRange(DateRange dateRange) {
      return dateRange.minus(7L);
    }

    @Override
    public boolean causesReset(ResetInterval interval) {
      return interval == ResetInterval.WEEKLY;
    }
  },

  /**
   * /shop challenges, reset daily
   */
  ITEMS("Item");

  @Getter
  private final String displayName;

  public DateRange searchRange(LocalDate date) {
    return DateRange.exact(date);
  }

  public DateRange moveRange(DateRange dateRange) {
    return dateRange.minus(1L);
  }

  public boolean causesReset(ResetInterval interval) {
    return interval == ResetInterval.DAILY;
  }
}