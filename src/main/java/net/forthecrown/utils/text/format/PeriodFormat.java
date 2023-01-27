package net.forthecrown.utils.text.format;

import it.unimi.dsi.fastutil.objects.ObjectList;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodFormat implements ComponentLike {
  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * All {@link ChronoUnit}s period formatting supports
   */
  private static final List<ChronoUnit> SUPPORTED_UNITS = ObjectList.of(
      ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.WEEKS,
      ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES,
      ChronoUnit.SECONDS, ChronoUnit.MILLIS
  );

  /**
   * Constant for the amount of milliseconds in a second
   */
  public final static long MILLIS_IN_SECOND = TimeUnit.SECONDS.toMillis(1);

  public static final long MILLIS_IN_MINUTE = TimeUnit.MINUTES.toMillis(1);

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * All unit entries and their values ordered in the order they'll be written
   */
  private final ChronoEntry[] entries;

  @Getter
  private final Style numberStyle, textStyle;

  @Getter
  private final boolean shortNames;

  /* ----------------------------- STATIC CONSTRUCTORS ------------------------------ */

  /**
   * Creates a new period format builder
   *
   * @return The created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a period format for the given time
   *
   * @param time The time to format
   * @return The created formatter
   */
  public static PeriodFormat of(long time) {
    Validate.isTrue(time > 0, "Time cannot be less than 0, found: %s", time);

    var currentTime = System.currentTimeMillis();
    return between(currentTime, currentTime + time);
  }

  /**
   * Creates a period format that formats the time as a difference between
   * {@link System#currentTimeMillis()} and the given time stamp
   *
   * @param time The time stamp to get the difference of
   * @return The created format
   */
  public static PeriodFormat timeStamp(long time) {
    var sysTime = System.currentTimeMillis();
    return between(
        Math.min(sysTime, time),
        Math.max(sysTime, time)
    );
  }

  /**
   * Creates a period format between the 2 given time stamps
   *
   * @param start The start time stamp
   * @param end   The end time stamp
   * @return The created period format
   */
  public static PeriodFormat between(long start, long end) {
    return between(Time.localTime(start), Time.localTime(end));
  }

  /**
   * Creates a period format between the two date times
   *
   * @param start The start time
   * @param end   The end time
   * @return The created format
   */
  public static PeriodFormat between(LocalDateTime start, LocalDateTime end) {
    var builder = builder();
    long time = start.until(end, ChronoUnit.MILLIS);

    // If less than a second of time between the start and ends,
    // then just create a format with only the millis unit
    if (time < MILLIS_IN_MINUTE) {
      builder.add(ChronoUnit.MILLIS, time);
    } else {
      // Otherwise loop through as many units as
      // possible and add their values to the format
      for (var unit : SUPPORTED_UNITS) {
        if (unit == ChronoUnit.MILLIS) {
          continue;
        }

        var amount = unit.between(start, end);
        start = start.plus(amount, unit);

        // difference does not contain
        // the given unit, skip
        if (amount <= 0) {
          continue;
        }

        time -= (unit.getDuration().toMillis() * amount);
        builder.add(unit, amount);

        // Less than a second of time left, end
        if (time < MILLIS_IN_MINUTE) {
          builder.add(ChronoUnit.MILLIS, time);
          break;
        }
      }
    }

    return builder.build();
  }

  /* ----------------------------- FORMAT METHODS ------------------------------ */

  /**
   * Formats this period's info into the given writer
   *
   * @param writer The writer to write to
   */
  public void format(TextWriter writer) {
    var it = new WriteIterator();

    while (it.hasNext()) {
      var entry = it.next();
      ChronoUnit unit = entry.unit;
      double amount = entry.amount;

      if (entry.unit == ChronoUnit.MILLIS) {
        double millis = entry.amount;
        amount = millis / MILLIS_IN_SECOND;
        unit = ChronoUnit.SECONDS;
      }

      writer.write(Text.formatNumber(amount).style(numberStyle));

      if (!shortNames) {
        writer.space();
        writer.write(Component.text(
            ChronoEntry.displayName(unit, entry.amount),
            textStyle
        ));
      } else {
        writer.write(
            ChronoEntry.shortDisplayName(unit, entry.amount),
            textStyle
        );
      }

      if (it.hasNext()) {
        writer.write(
            // If next entry is final, use 'and', else use ','
            // propah english grammar that is
            Component.text(
                (it.isNextFinal() && !shortNames)
                    ? " and "
                    : ", ",

                textStyle
            )
        );
      }
    }
  }

  @Override
  public @NotNull Component asComponent() {
    var writer = TextWriters.newWriter();
    format(writer);

    return writer.asComponent();
  }

  @Override
  public String toString() {
    var writer = TextWriters.stringWriter();
    format(writer);

    return writer.getPlain();
  }

  /* ----------------------------- BUILDER METHODS ------------------------------ */

  /**
   * Creates a format with all entries smaller than the given unit.
   * <p>
   * If this format only has 1 entry, then this method will return itself.
   *
   * @param unit The unit to remove all lower values of
   * @return The created format
   */
  public PeriodFormat withoutLower(ChronoUnit unit) {
    // Prevent removal of units if we're already
    // showing only 1 entry
    if (entries.length == 1) {
      return this;
    }

    for (int i = 0; i < entries.length; i++) {
      if (entries[i].unit.ordinal() >= unit.ordinal()) {
        continue;
      }

      return new PeriodFormat(
          ArrayUtils.subarray(entries, 0, i),
          numberStyle,
          textStyle,
          shortNames
      );
    }

    return this;
  }

  /**
   * Creates a copy of this format without the given unit's entry
   *
   * @param unit The unit to remove
   * @return The created format
   */
  public PeriodFormat without(ChronoUnit unit) {
    var index = indexOf(unit);

    if (index == -1) {
      return this;
    }

    return new PeriodFormat(
        ArrayUtils.remove(entries, index),
        numberStyle,
        textStyle,
        shortNames
    );
  }

  /**
   * Creates a new format with only the given units, all others are discarded.
   *
   * @param units The units to retain
   * @return The created format
   */
  public PeriodFormat retain(ChronoUnit... units) {
    var entries = this.entries;

    for (var e : this.entries) {
      if (ArrayUtils.contains(units, e.unit)) {
        continue;
      }

      entries = ArrayUtils.removeElement(entries, e);
    }

    return new PeriodFormat(entries, numberStyle, textStyle, shortNames);
  }

  /**
   * Creates a new format with only the largest unit
   *
   * @return The created format
   */
  public PeriodFormat retainBiggest() {
    if (entries.length < 2) {
      return this;
    }

    return new PeriodFormat(
        ArrayUtils.subarray(entries, 0, 1),
        numberStyle,
        textStyle,
        shortNames
    );
  }

  private int indexOf(ChronoUnit unit) {
    for (int i = 0; i < entries.length; i++) {
      if (entries[i].unit == unit) {
        return i;
      }
    }

    return -1;
  }

  public PeriodFormat withShortNames() {
    return new PeriodFormat(entries, numberStyle, textStyle, true);
  }

  public PeriodFormat withLongNames() {
    return new PeriodFormat(entries, numberStyle, textStyle, false);
  }

  /* ----------------------------- SUB CLASSES ------------------------------ */

  private class WriteIterator implements Iterator<ChronoEntry> {

    int index = 0;

    boolean isNextFinal() {
      return nextNonEmpty(this.index + 1) >= entries.length;
    }

    @Override
    public boolean hasNext() {
      return (index = nextNonEmpty(index)) < entries.length;
    }

    private int nextNonEmpty(int index) {
      while (index < entries.length &&
          (entries[index] == null || entries[index].amount <= 0)
      ) {
        index++;
      }

      return index;
    }

    @Override
    public ChronoEntry next() {
      return entries[index++];
    }
  }

  private record ChronoEntry(ChronoUnit unit, long amount) implements Comparable<ChronoEntry> {

    public static String displayName(ChronoUnit unit, double amount) {
      String name = unit.toString();
      return amount == 1 ? name.replaceAll("(s$)", "") : name;
    }

    public static String shortDisplayName(ChronoUnit unit, double amount) {
      if (unit == ChronoUnit.MILLIS) {
        return "millis";
      }

      return displayName(unit, amount)
          .toLowerCase()
          .substring(0, 1);
    }

    String displayName() {
      return displayName(unit, amount);
    }

    String shortDisplayName() {
      return shortDisplayName(unit, amount);
    }

    @Override
    public int compareTo(@NotNull PeriodFormat.ChronoEntry o) {
      return -(unit.compareTo(o.unit));
    }
  }

  @Setter
  @Accessors(chain = true)
  public static class Builder {

    private Style numberStyle = Style.empty();
    private Style textStyle = Style.empty();
    private final EnumMap<ChronoUnit, Long> entries
        = new EnumMap<>(ChronoUnit.class);

    private boolean shortNames;

    public Builder add(ChronoUnit unit, long amount) {
      Validate.isTrue(SUPPORTED_UNITS.contains(unit), "Not a supported unit");
      entries.put(unit, amount);
      return this;
    }

    public PeriodFormat build() {
      ChronoEntry[] entries = new ChronoEntry[this.entries.size()];

      int index = 0;
      for (var e : this.entries.entrySet()) {
        entries[index++] = new ChronoEntry(e.getKey(), e.getValue());
      }

      Arrays.sort(entries);

      return new PeriodFormat(
          entries,
          numberStyle,
          textStyle,
          shortNames
      );
    }
  }
}