package net.forthecrown.core.holidays;

import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * Represents the period of time in which a holiday is active,
 * or just the date its active
 */
@Data
public class MonthDayPeriod {
    /**
     * Date time formatter used to parse values and to format them
     * for serialization into NBT
     */
    public static final DateTimeFormatter SERIAL_FORMATTER = new DateTimeFormatterBuilder()
            .appendLiteral("date=")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral(",month=")
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .toFormatter(Locale.ENGLISH);

    /**
     * Date time formatter used to display date values to users
     */
    public static final DateTimeFormatter DISPLAY_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .toFormatter(Locale.ENGLISH);

    /**
     * The start date of this period.
     * <p>
     * If {@link #isExact()} returns true,
     * then this determines the date of
     * the holiday, and {@link #end} will
     * be null.
     */
    private final MonthDay start;

    /**
     * The end date of this period.
     * <p>
     * This maybe null, that means that this
     * is an exact date period, meaning it only
     * lasts a single day. And in that case,
     * {@link #start} will not be null.
     * <p>
     * {@link #isExact()} can be used to easily
     * check if this value is null, aka, if
     * this is an exact period.
     */
    private final MonthDay end;

    /**
     * Loads a period from the given tag
     *
     * @param t The tag to load from
     * @return The loaded tag
     */
    public static MonthDayPeriod load(Tag t) {
        CompoundTag tag = (CompoundTag) t;

        if (tag.contains("date")) {
            return exact(
                    MonthDay.parse(tag.getString("date"), SERIAL_FORMATTER)
            );
        } else {
            return between(
                    MonthDay.parse(tag.getString("start"), SERIAL_FORMATTER),
                    MonthDay.parse(tag.getString("end"), SERIAL_FORMATTER)
            );
        }
    }

    /**
     * Gets a period for a single date of a year
     *
     * @param month The month of the holiday
     * @param date  The day-of-month of the holiday
     * @return The created period
     */
    public static MonthDayPeriod exact(Month month, int date) {
        return new MonthDayPeriod(MonthDay.of(month, date), null);
    }

    /**
     * Creates a holiday period that's only
     * active on the given month-day
     * @param monthDay The holiday date
     * @return The created period
     */
    public static MonthDayPeriod exact(MonthDay monthDay) {
        return new MonthDayPeriod(monthDay, null);
    }

    /**
     * Creates a period that would be active between the given dates
     *
     * @param m1    The starting month
     * @param start The starting day-of-month
     * @param m2    The ending month
     * @param end   The ending day-of-month
     * @return The created period
     */
    public static MonthDayPeriod between(Month m1, int start, Month m2, int end) {
        return new MonthDayPeriod(
                MonthDay.of(m1, start),
                MonthDay.of(m2, end)
        );
    }

    /**
     * Creates a period between the given
     * month days
     * @param start The start month-day
     * @param end The end month-day
     * @return The created holiday period
     */
    public static MonthDayPeriod between(MonthDay start, MonthDay end) {
        return new MonthDayPeriod(start, end);
    }

    /**
     * Checks if this period should be active right now
     *
     * @param time The timezone to use
     * @return True, if this holiday should be active right now
     */
    public boolean contains(ZonedDateTime time) {
        if (isExact()) {
            return start.getDayOfMonth() == time.getDayOfMonth()
                    && start.getMonthValue() == time.getMonthValue();
        }

        var month = time.getMonthValue();
        var date = time.getDayOfMonth();

        if (month < start.getDayOfMonth() || month > end.getMonthValue()) {
            return false;
        }

        return date >= start.getDayOfMonth() && date <= end.getDayOfMonth();
    }

    /**
     * Checks if this period only lasts a single day
     *
     * @return True, if the period only lasts a single day
     */
    public boolean isExact() {
        return end == null;
    }

    public Tag save() {
        CompoundTag tag = new CompoundTag();

        if (isExact()) {
            tag.putString("date", SERIAL_FORMATTER.format(start));
        } else {
            tag.putString("start", SERIAL_FORMATTER.format(start));
            tag.putString("end", SERIAL_FORMATTER.format(end));
        }

        return tag;
    }

    @Override
    public String toString() {
        if (isExact()) {
            return start.format(DISPLAY_FORMATTER);
        }

        return start.format(DISPLAY_FORMATTER) + " - " + end.format(DISPLAY_FORMATTER);
    }
}