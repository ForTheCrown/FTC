package net.forthecrown.log;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@Getter
public class DateRange implements Iterable<LocalDate> {
  public static final long MILLIS_PER_DAY = 86_400_000;

  public static final int BEFORE = -1;
  public static final int INSIDE =  0;
  public static final int AFTER =   1;

  private final long minEpoch;
  private final long maxEpoch;

  private final long millisMin;
  private final long millisMax;

  public DateRange(long minEpoch, long maxEpoch) {
    this.minEpoch = Math.min(minEpoch, maxEpoch);
    this.maxEpoch = Math.max(minEpoch, maxEpoch);

    this.millisMin = MILLIS_PER_DAY * this.minEpoch;
    this.millisMax = (MILLIS_PER_DAY * this.maxEpoch) + MILLIS_PER_DAY;
  }

  public static DateRange between(ChronoLocalDate d1, ChronoLocalDate d2) {
    return new DateRange(d1.toEpochDay(), d2.toEpochDay());
  }

  public static DateRange exact(ChronoLocalDate d) {
    long epoch = d.toEpochDay();
    return new DateRange(epoch, epoch);
  }

  public boolean isExact() {
    return minEpoch == maxEpoch;
  }

  public DateRange overlap(DateRange o) {
    Validate.isTrue(overlaps(o), "Not overlapping");
    return new DateRange(
        Math.max(minEpoch, o.minEpoch),
        Math.min(maxEpoch, o.maxEpoch)
    );
  }

  public boolean overlaps(DateRange other) {
    return other.getMaxEpoch() >= minEpoch
        && other.getMinEpoch() <= maxEpoch;
  }

  public boolean overlaps(Range<ChronoLocalDate> range) {
    long rMin = range.getMinimum().toEpochDay();
    long rMax = range.getMaximum().toEpochDay();
    return rMax >= minEpoch && rMin <= maxEpoch;
  }

  public DateRange encompassing(LocalDate d) {
    long epoch = d.toEpochDay();
    return new DateRange(
        Math.min(minEpoch, epoch),
        Math.max(maxEpoch, epoch)
    );
  }

  public DateRange minus(long days) {
    return new DateRange(
        minEpoch - days,
        maxEpoch - days
    );
  }

  public int containsMillis(long millis) {
    return (millis < millisMin) ? BEFORE : (millis > millisMax) ? AFTER : INSIDE;
  }

  public String millisToString() {
    return Strings.lenientFormat("[%s..%s]", millisMin, millisMax);
  }

  @NotNull
  @Override
  public Iterator<LocalDate> iterator() {
    return new Iterator<>() {
      long date = maxEpoch;

      @Override
      public boolean hasNext() {
        return date >= minEpoch && date <= maxEpoch;
      }

      @Override
      public LocalDate next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        return LocalDate.ofEpochDay(date--);
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DateRange range)) {
      return false;
    }
    return getMinEpoch() == range.getMinEpoch()
        && getMaxEpoch() == range.getMaxEpoch();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMinEpoch(), getMaxEpoch());
  }

  @Override
  public String toString() {
    LocalDate min = LocalDate.ofEpochDay(minEpoch);

    if (isExact()) {
      return "[" + min + "]";
    }

    LocalDate max = LocalDate.ofEpochDay(maxEpoch);
    return "[" + min + ".." + max + "]";
  }
}