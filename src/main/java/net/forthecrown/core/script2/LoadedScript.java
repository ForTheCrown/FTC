package net.forthecrown.core.script2;

import java.time.LocalDate;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.MonthDayPeriod;
import org.apache.logging.log4j.Logger;

record LoadedScript(MonthDayPeriod period, Script script, String[] args) {
  private static final Logger LOGGER = Loggers.getLogger();

  public boolean shouldBeActive(LocalDate date) {
    if (period == null) {
      return true;
    }

    return period.contains(date);
  }

  public void load() {
    try {
      _load();
    } catch (Exception ex) {
      LOGGER.error("Couldn't compile script {}", script, ex);
    }
  }

  public void _load() {
    script.compile();

    if (args != null) {
      script.put("inputs", args);
    }

    if (script.eval().error().isEmpty()) {
      LOGGER.debug("Activated script {}", script);
    }
  }
}