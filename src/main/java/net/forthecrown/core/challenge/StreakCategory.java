package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Range;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.*;

/**
 * A category for streaks, each challenge selects its own category.
 * <p>
 * For a player to get a streak in a category, it must complete all challenges
 * in a streak category in an allowed time frame.
 */
@RequiredArgsConstructor
public enum StreakCategory implements TemporalAdjuster {
    /** Challenges which reset daily */
    DAILY ("Daily"),

    /** Challenges which reset weekly */
    WEEKLY ("Weekly") {
        @Override
        public Range<ChronoLocalDate> searchRange(LocalDate date) {
            var start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            var end = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            return Range.between(start, end);
        }

        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.minus(7, ChronoUnit.DAYS);
        }
    },

    /** /shop challenges, reset daily */
    ITEMS ("Item");

    @Getter
    private final String displayName;

    public Range<ChronoLocalDate> searchRange(LocalDate date) {
        return Range.is(date);
    }

    public Range<ChronoLocalDate> moveRange(Range<ChronoLocalDate> dateRange) {
        return Range.between(
                dateRange.getMinimum().with(this),
                dateRange.getMaximum().with(this)
        );
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.minus(1L, ChronoUnit.DAYS);
    }
}