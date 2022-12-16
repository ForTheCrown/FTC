package net.forthecrown.utils;

import net.forthecrown.utils.MonthDayPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthDayPeriodTest {

    @Test
    void contains() {
        MonthDayPeriod decToJan = MonthDayPeriod.between(
                MonthDay.of(Month.DECEMBER, 1),
                MonthDay.of(Month.JANUARY, 31)
        );

        LocalDate yes1 = LocalDate.of(2019, Month.DECEMBER, 13);
        LocalDate yes2 = LocalDate.of(2020, Month.JANUARY, 10);

        LocalDate no1 = LocalDate.of(2018, Month.FEBRUARY, 2);

        assertTrue(decToJan.contains(yes1));
        assertTrue(decToJan.contains(yes2));

        assertFalse(decToJan.contains(no1));
    }
}