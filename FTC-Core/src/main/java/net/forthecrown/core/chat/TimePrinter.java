package net.forthecrown.core.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicLong;

public class TimePrinter implements ComponentPrinter {
    public static final int
            ORDER_SMALLEST_TO_BIGGEST = -1,
            ORDER_BIGGEST_TO_SMALLEST =  1;

    private final long time;
    private boolean valueWritten;
    private boolean finalValue;
    private int order;

    private final UnitEntry[] entries;
    private final StringBuilder builder = new StringBuilder();

    public TimePrinter(long time) {
        Validate.isTrue(time > 0, "Time value cannot be less than 1, was: " + time);

        this.time = time;
        entries = calculateUnits(time);

        setOrder(ORDER_BIGGEST_TO_SMALLEST);
    }

    public static UnitEntry[] calculateUnits(long millisTime) {
        AtomicLong time = new AtomicLong(millisTime);
        UnitEntry[] result = new UnitEntry[TimerUnit.REVERSE_ORDER.length];

        for (int i = 0; i < TimerUnit.REVERSE_ORDER.length; i++) {
            TimerUnit unit = TimerUnit.REVERSE_ORDER[i];
            result[i] = calculate(time, unit);
        }

        return result;
    }

    private static UnitEntry calculate(AtomicLong time, TimerUnit unit) {
        long inMillis = unit.getInMillis();
        long result = time.get() / inMillis;
        time.addAndGet(-(result * inMillis));

        return new UnitEntry(unit, result);
    }

    public long getTime() {
        return time;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    private TimePrinter add(UnitEntry entry) {
        if(valueWritten) {
            write(finalValue ? " and " : ", ");
        }

        valueWritten = true;
        return write(entry.time + " " + entry.unit.name().toLowerCase() + FtcUtils.addAnS(entry.time));
    }

    private TimePrinter write(String val) {
        builder.append(val);
        return this;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        Validate.isTrue(order == ORDER_BIGGEST_TO_SMALLEST || order == ORDER_SMALLEST_TO_BIGGEST, "Invalid order");
        this.order = order;
    }

    public TimePrinter build() {
        return build(TimerUnit.REVERSE_ORDER.length);
    }

    public TimePrinter build(int maxUnits) {
        int start = order == ORDER_BIGGEST_TO_SMALLEST ? 0 : entries.length - 1;
        int moveDir = order;
        int units = 0;

        for (int i = start; inList(i); i += moveDir) {
            UnitEntry entry = entries[i];
            if(entry.time <= 0) continue;
            if(units >= maxUnits) continue;

            if(entry.unit == TimerUnit.MILLISECOND && time >= TimerUnit.SECOND.inMillis) {
                continue;
            }

            units++;
            checkFinalValue(i, moveDir);
            if(units >= maxUnits) finalValue = true;

            add(entry);
        }

        return this;
    }

    private void checkFinalValue(int start, int dir) {
        start += dir;
        int nonZeroCount = 0;

        for (int i = start; inList(i); i += dir) {
            if(!inList(i)) continue;

            UnitEntry entry = entries[i];

            if(entry.unit == TimerUnit.MILLISECOND && time >= TimerUnit.SECOND.inMillis) {
                continue;
            }

            nonZeroCount += entry.time > 0 ? 1 : 0;
        }

        finalValue = nonZeroCount == 0;
    }

    private boolean inList(int i) {
        return i >= 0 && i < entries.length;
    }

    private TimePrinter buildBiggest() {
        for (UnitEntry e: entries) {
            if(e.time <= 0) continue;

            add(e);
            return this;
        }

        return this;
    }

    @Override
    public Component print() {
        return Component.text(printString());
    }

    public String printString() {
        return build().printCurrentString();
    }

    public Component printBiggest() {
        return Component.text(printStringBiggest());
    }

    public String printStringBiggest() {
        return buildBiggest().printCurrentString();
    }

    @Override
    public Component printCurrent() {
        return Component.text(printCurrentString());
    }

    public String printCurrentString() {
        String s = toString();
        clear();

        return s;
    }

    public String toString() {
        return builder.toString();
    }

    private void clear() {
        builder.delete(0, builder.length());
        finalValue = false;
        valueWritten = false;
    }

    public static class UnitEntry {
        final TimerUnit unit;
        long time;

        public UnitEntry(TimerUnit unit, long time) {
            this.unit = unit;
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public TimerUnit getUnit() {
            return unit;
        }
    }

    @RequiredArgsConstructor
    public enum TimerUnit {
        MILLISECOND (1L),
        SECOND      (TimeUtil.SECOND_IN_MILLIS),
        MINUTE      (TimeUtil.MINUTE_IN_MILLIS),
        HOUR        (TimeUtil.HOUR_IN_MILLIS),
        DAY         (TimeUtil.DAY_IN_MILLIS),
        WEEK        (TimeUtil.WEEK_IN_MILLIS),
        MONTH       (TimeUtil.MONTH_IN_MILLIS),
        YEAR        (TimeUtil.YEAR_IN_MILLIS);

        static final TimerUnit[] REVERSE_ORDER = Util.make(() -> {
            TimerUnit[] values = values().clone();
            ArrayUtils.reverse(values);

            return values;
        });

        @Getter
        final long inMillis;
    }
}