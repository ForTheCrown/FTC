package net.forthecrown.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Objects;

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
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
            .appendLiteral('.')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
            .toFormatter();

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

    /* ------------------------------ PARSING ------------------------------- */

    /**
     * Parses an exact monthday period which only contains the given date.
     *
     * @param sequence The sequence to parse
     * @return The parsed period
     *
     * @throws DateTimeParseException If the sequence couldn't be parsed
     * @throws NullPointerException If the sequence was null
     */
    public static MonthDayPeriod parse(CharSequence sequence)
            throws DateTimeParseException, NullPointerException
    {
        return exact(parseMonthDay(sequence));
    }

    /**
     * Parses a month day period between 2 given dates.
     *
     * @see #parseMonthDay(CharSequence)
     *
     * @param start The start date to parse
     * @param end The end date to parse
     *
     * @return The parsed period
     *
     * @throws NullPointerException If either start or end parameters were null
     * @throws DateTimeParseException If either the start or end couldn't be
     *                                parsed.
     */
    public static MonthDayPeriod parse(CharSequence start, CharSequence end)
            throws NullPointerException, DateTimeParseException
    {
        return between(
                parseMonthDay(start),
                parseMonthDay(end)
        );
    }

    /**
     * Parses a single MonthDay instance.
     * <p>
     * This method accepts a specific format for parsing, that being 'DD.MM'
     * examples of accepted input are:
     * <pre>
     * - 01.12
     * - 31.12
     * - 1.5
     * - 01.05
     * </pre>
     *
     * @param sequence The char sequence to parse
     *
     * @return The parsed month day
     *
     * @throws DateTimeParseException If the monthday couldn't be parsed
     * @throws NullPointerException If the sequence is null
     */
    public static MonthDay parseMonthDay(CharSequence sequence)
            throws DateTimeParseException, NullPointerException
    {
        return MonthDay.parse(Objects.requireNonNull(sequence), PARSER);
    }

    /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

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

    /* ------------------------------ METHODS ------------------------------- */

    /**
     * Checks if this period should be active right now
     *
     * @param time The timezone to use
     * @return True, if this holiday should be active right now
     */
    public boolean contains(LocalDate time) {
        LocalDate startDate = LocalDate.of(
                time.getYear(), start.getMonth(), start.getDayOfMonth()
        );

        if (end == null) {
            return startDate.compareTo(time) == 0;
        }

        LocalDate endDate = LocalDate.of(
                time.getYear(), end.getMonth(), end.getDayOfMonth()
        );

        // End takes place on the year after start
        if (end.isBefore(start)) {
            if (time.isBefore(endDate)) {
                startDate = startDate.minusYears(1);
            } else {
                endDate = endDate.plusYears(1);
            }
        }

        long minEpoch = startDate.getLong(ChronoField.EPOCH_DAY);
        long maxEpoch = endDate.getLong(ChronoField.EPOCH_DAY);
        long timeEpoch = time.getLong(ChronoField.EPOCH_DAY);

        return timeEpoch <= maxEpoch
                && timeEpoch >= minEpoch;
    }

    /**
     * Checks if this period only lasts a single day
     *
     * @return True, if the period only lasts a single day
     */
    public boolean isExact() {
        return end == null || start.compareTo(end) == 0;
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public Tag save() {
        CompoundTag tag = new CompoundTag();

        if (isExact()) {
            tag.putString("date", PARSER.format(start));
        } else {
            tag.putString("start", PARSER.format(start));
            tag.putString("end", PARSER.format(end));
        }

        return tag;
    }

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
                    MonthDay.parse(tag.getString("date"), PARSER)
            );
        } else {
            return between(
                    MonthDay.parse(tag.getString("start"), PARSER),
                    MonthDay.parse(tag.getString("end"), PARSER)
            );
        }
    }

    public static MonthDayPeriod load(JsonElement element) {
        Objects.requireNonNull(element);

        if (element.isJsonPrimitive()) {
            return parse(element.getAsString());
        }

        var array = element.getAsJsonArray();

        if (array.size() > 2 || array.isEmpty()) {
            throw Util.newException(
                    "Expected size [1, 2], found: %s", array.size()
            );
        }

        String start = array.get(0).getAsString();

        if (array.size() == 1) {
            return parse(start);
        } else {
            String end = array.get(1).getAsString();
            return parse(start, end);
        }
    }

    public JsonElement saveAsJson() {
        if (isExact()) {
            return new JsonPrimitive(toString());
        }

        JsonArray array = new JsonArray(2);
        array.add(start.format(PARSER));
        array.add(end.format(PARSER));
        return array;
    }

    /* -------------------------- OBJECT OVERRIDES -------------------------- */

    @Override
    public String toString() {
        if (isExact()) {
            return start.format(PARSER);
        }

        return start.format(PARSER) + " - " + end.format(PARSER);
    }
}