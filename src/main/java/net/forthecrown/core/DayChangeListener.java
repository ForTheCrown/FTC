package net.forthecrown.core;

import java.time.ZonedDateTime;

/**
 * Functional interface called by {@link DayChange} when
 * the day changes
 */
@FunctionalInterface
public interface DayChangeListener {
    /**
     * Called when the date changes
     * @param time The current time
     */
    void onDayChange(ZonedDateTime time);
}