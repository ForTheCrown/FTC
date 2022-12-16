package net.forthecrown.utils;

import net.kyori.adventure.util.Ticks;

import java.time.*;

/** A class with utility functions relating to time. */
public final class Time {
    private Time() {}

    public static long millisToTicks(long millis) {
        return millis / Ticks.SINGLE_TICK_DURATION_MS;
    }

    public static long ticksToMillis(long ticks) {
        return ticks * Ticks.SINGLE_TICK_DURATION_MS;
    }

    public static long timeSince(long timeStamp) {
        return System.currentTimeMillis() - timeStamp;
    }

    public static boolean isPast(long timeStamp) {
        return timeStamp <= System.currentTimeMillis();
    }

    public static long timeUntil(long timeStamp) {
        return timeStamp - System.currentTimeMillis();
    }

    public static ZonedDateTime dateTime(long timeStamp) {
        return ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(timeStamp),
                ZoneId.systemDefault()
        );
    }

    public static LocalDate localDate(long time) {
        return LocalDate.ofInstant(
                Instant.ofEpochMilli(time),
                ZoneId.systemDefault()
        );
    }

    public static LocalDateTime localTime(long timeStamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timeStamp),
                ZoneId.systemDefault()
        );
    }

    public static long toTimestamp(ZonedDateTime time) {
        return time.toInstant().toEpochMilli();
    }
}