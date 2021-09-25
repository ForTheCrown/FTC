package net.forthecrown.utils;

public final class TimeUtil {
    private TimeUtil() {}

    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
    public static final long MONTH_IN_MILLIS = WEEK_IN_MILLIS * 4;

    public static long millisToTicks(long millis) {
        return millis / 50;
    }

    public static long ticksToMillis(long ticks) {
        return ticks * 50;
    }
}