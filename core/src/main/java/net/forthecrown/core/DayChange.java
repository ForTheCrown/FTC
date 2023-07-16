package net.forthecrown.core;

import java.time.Duration;
import java.time.ZonedDateTime;
import net.forthecrown.Loggers;
import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.text.PeriodFormat;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

public class DayChange implements Runnable {

  public static final Logger LOGGER = Loggers.getLogger();

  private BukkitTask task;

  public void schedule() {
    stop();

    long nextDay = Time.getNextDayChange();
    Duration until = Duration.ofMillis(Time.timeUntil(nextDay));
    task = Tasks.runTimer(this, until, Duration.ofHours(24));

    LOGGER.debug("Executing in {}", PeriodFormat.of(until));
  }

  public void stop() {
    task = Tasks.cancel(task);
  }

  @Override
  public void run() {
    ZonedDateTime time = ZonedDateTime.now();
    DayChangeEvent event = new DayChangeEvent(time);
    event.callEvent();
  }
}
