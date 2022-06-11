package net.forthecrown.core;

/**
 * Functional interface called by {@link DayChange} when
 * the day changes
 */
@FunctionalInterface
public interface DayChangeListener {
    /**
     * Called when the date changes
     */
    void onDayChange();
}