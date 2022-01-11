package net.forthecrown.core.chat;

import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicLong;

public class TimePrinter implements ComponentPrinter {
    private final long time;
    private boolean valueWritten;
    private boolean finalValue;

    private int millis, seconds, minutes, hours, days, months, years;

    private final StringBuilder builder = new StringBuilder();

    public TimePrinter(long time) {
        Validate.isTrue(time > 0, "Time value cannot be less than 1, was: " + time);

        this.time = time;
        calculateUnits();
    }

    private void calculateUnits() {
        AtomicLong time = new AtomicLong(this.time);

        years = calculate(time, TimeUtil.YEAR_IN_MILLIS);
        months = calculate(time, TimeUtil.MONTH_IN_MILLIS);
        days = calculate(time, TimeUtil.DAY_IN_MILLIS);
        hours = calculate(time, TimeUtil.HOUR_IN_MILLIS);
        minutes = calculate(time, TimeUtil.MINUTE_IN_MILLIS);
        seconds = calculate(time, TimeUtil.SECOND_IN_MILLIS);

        millis = time.intValue();
    }

    private int calculate(AtomicLong time, long inMillis) {
        int result = (int) (time.get() / inMillis);
        time.addAndGet(-(result * inMillis));

        return result;
    }

    public long getTime() {
        return time;
    }

    public int getMillis() {
        return millis;
    }

    public TimePrinter setMillis(int millis) {
        this.millis = millis;
        return this;
    }

    public int getSeconds() {
        return seconds;
    }

    public TimePrinter setSeconds(int seconds) {
        this.seconds = seconds;
        return this;
    }

    public int getMinutes() {
        return minutes;
    }

    public TimePrinter setMinutes(int minutes) {
        this.minutes = minutes;
        return this;
    }

    public int getHours() {
        return hours;
    }

    public TimePrinter setHours(int hours) {
        this.hours = hours;
        return this;
    }

    public int getDays() {
        return days;
    }

    public TimePrinter setDays(int days) {
        this.days = days;
        return this;
    }

    public int getMonths() {
        return months;
    }

    public TimePrinter setMonths(int months) {
        this.months = months;
        return this;
    }

    public int getYears() {
        return years;
    }

    public TimePrinter setYears(int years) {
        this.years = years;
        return this;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    public TimePrinter addMillis() {
        finalValue = areZero(seconds, minutes, hours, days, months, years);
        return add(millis, "millisecond");
    }

    public TimePrinter addSeconds() {
        finalValue = areZero(minutes, hours, days, months, years);
        return add(seconds, "second");
    }

    public TimePrinter addMinutes() {
        finalValue = areZero(hours, days, months, years);
        return add(minutes, "minute");
    }

    public TimePrinter addHours() {
        finalValue = areZero(days, months, years);
        return add(hours, "hour");
    }

    public TimePrinter addDays() {
        finalValue = areZero(months, years);
        return add(days, "day");
    }

    public TimePrinter addMonths() {
        finalValue = areZero(years);
        return add(months, "month");
    }

    public TimePrinter addYears() {
        finalValue = true;
        return add(years, "year");
    }

    private TimePrinter add(int val, String field) {
        if(valueWritten) {
            write(finalValue ? " and " : ", ");
        }

        valueWritten = val > 0;
        return write(val + " " + field + FtcUtils.addAnS(val));
    }

    private TimePrinter write(String val) {
        builder.append(val);
        return this;
    }

    private boolean areZero(int... values) {
        for (int i: values) {
            if(i > 0) return false;
        }

        return true;
    }

    private void build() {
        if (time < TimeUtil.SECOND_IN_MILLIS) {
            addMillis();
            return;
        }

        if (seconds > 0) addSeconds();
        if (minutes > 0) addMinutes();
        if (hours > 0) addHours();
        if (days > 0) addDays();
        if (months > 0) addMonths();
        if (years > 0) addYears();
    }

    private void buildBiggest() {
        if(months > 0) addMonths();
        else if(days > 0) addDays();
        else if(hours > 0) addHours();
        else if(minutes > 0) addMinutes();
        else if(seconds > 0) addSeconds();
        else if(millis > 0) addMillis();
    }

    @Override
    public Component print() {
        build();
        return printCurrent();
    }

    public String printString() {
        build();
        return toString();
    }

    public Component printBiggest() {
        buildBiggest();
        return printCurrent();
    }

    public String printStringBiggest() {
        buildBiggest();
        return toString();
    }

    @Override
    public Component printCurrent() {
        return Component.text(builder.toString());
    }

    public String toString() {
        return builder.toString();
    }
}
