package net.forthecrown.core.script2;

import java.time.LocalDate;
import net.forthecrown.utils.MonthDayPeriod;

record LoadedScript(MonthDayPeriod period, Script script, String[] args) {
  public boolean shouldBeActive(LocalDate date) {
    if (period == null) {
      return true;
    }

    return period.contains(date);
  }
}