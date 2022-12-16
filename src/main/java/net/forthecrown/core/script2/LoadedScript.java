package net.forthecrown.core.script2;

import net.forthecrown.utils.MonthDayPeriod;

import java.time.LocalDate;

record LoadedScript(MonthDayPeriod period, Script script) {
    public boolean shouldBeActive(LocalDate date) {
        if (period == null) {
            return true;
        }

        return period.contains(date);
    }
}