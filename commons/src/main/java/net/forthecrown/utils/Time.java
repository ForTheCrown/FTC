package net.forthecrown.utils;

import static java.lang.System.currentTimeMillis;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import net.kyori.adventure.util.Ticks;

/**
 * A class with utility functions relating to time.
 */
public final class Time {
  private Time() {}

  public static final TemporalAdjuster NEXT_DAY = temporal -> {
    return temporal.plus(1, DAYS)
        .with(HOUR_OF_DAY, 0)
        .with(MINUTE_OF_HOUR, 0)
        .with(SECOND_OF_MINUTE, 0)
        .with(MILLI_OF_SECOND, 1);
  };

  public static long millisToTicks(long millis) {
    return millis / Ticks.SINGLE_TICK_DURATION_MS;
  }

  public static long ticksToMillis(long ticks) {
    return ticks * Ticks.SINGLE_TICK_DURATION_MS;
  }

  public static long timeSince(long timeStamp) {
    return currentTimeMillis() - timeStamp;
  }

  public static boolean isPast(long timeStamp) {
    return timeStamp <= currentTimeMillis();
  }

  public static long timeUntil(long timeStamp) {
    return timeStamp - currentTimeMillis();
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

  public static long getNextDayChange() {
    ZonedDateTime time = ZonedDateTime.now();
    time = time.with(NEXT_DAY);
    return toTimestamp(time);
  }
}