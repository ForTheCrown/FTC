package net.forthecrown.log;

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
  private final long minEpoch;
  private final long maxEpoch;

  public DateRange(long minEpoch, long maxEpoch) {
    this.minEpoch = Math.min(minEpoch, maxEpoch);
    this.maxEpoch = Math.max(minEpoch, maxEpoch);
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