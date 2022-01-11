package net.forthecrown.utils;

public final class TimeUtil {
    private TimeUtil() {}

    public static final long
            TICKS_IN_MILLIS     = 50,
            SECOND_IN_MILLIS    = 1000,
            MINUTE_IN_MILLIS    = SECOND_IN_MILLIS * 60,
            HOUR_IN_MILLIS      = MINUTE_IN_MILLIS * 60,
            DAY_IN_MILLIS       = HOUR_IN_MILLIS * 24,
            WEEK_IN_MILLIS      = DAY_IN_MILLIS * 7,
            MONTH_IN_MILLIS     = WEEK_IN_MILLIS * 4,
            YEAR_IN_MILLIS      = DAY_IN_MILLIS * 365;

    public static long millisToTicks(long millis) {
        return millis / TICKS_IN_MILLIS;
    }

    public static long ticksToMillis(long ticks) {
        return ticks * TICKS_IN_MILLIS;
    }

    public static long timeSince(long timeStamp) {
        return System.currentTimeMillis() - timeStamp;
    }

    public static boolean isPast(long timeStamp) {
        return timeStamp <= System.currentTimeMillis();
    }

    public static boolean hasCooldownEnded(long length, long started) {
        return isPast(length + started);
    }

    public static long timeUntil(long timeStamp) {
        return timeStamp - System.currentTimeMillis();
    }
}