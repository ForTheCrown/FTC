package net.forthecrown.core;

import lombok.Getter;
import net.forthecrown.text.format.PeriodFormat;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import org.bukkit.scheduler.BukkitTask;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DayUpdate listens to a change in the day.
 * <p>
 * <b>Word of warning</b>: Don't use the {@link Calendar} in the future,
 * use {@link java.time.ZonedDateTime}, it's more up to date and
 * easier to use and understand
 * </p>
 */
public class DayChange {
    private static final DayChange INSTANCE = new DayChange();

    @Getter
    private final List<DayChangeListener> listeners = new ArrayList<>();

    private BukkitTask updateTask;

    public static DayChange get() {
        return INSTANCE;
    }

    private void changeDay() {
        Crown.logger().info("Updating date");

        ZonedDateTime time = ZonedDateTime.now();
        listeners.forEach(r -> {
            try {
                r.onDayChange(time);
            } catch (Throwable e){
                Crown.logger().error("Could not update date of " + r.getClass().getSimpleName(), e);
            }
        });
    }

    void schedule() {
        // Cancel if previous task exists
        updateTask = Tasks.cancel(updateTask);
        // Find difference between now and tomorrow
        long difference = Time.timeUntil(getNextDayChange());

        Crown.logger().info("DayUpdate scheduled, executing in: {}", PeriodFormat.of(difference));

        // Convert to ticks for bukkit scheduler
        difference = Time.millisToTicks(difference);

        // Run update on next day change and then run it every
        // 24 hours, aka once a day. It probably won't get ran
        // a second time cuz of daily restart, but whatever lol
        // future-proof :D
        updateTask = Tasks.runTimer(this::changeDay, difference, Time.millisToTicks(TimeUnit.DAYS.toMillis(1)));
    }

    public static long getNextDayChange() {
        // Configure calendar to be at the start of the next day
        // So we can run day update exactly on time. As always,
        // there's probably a better way of doing this, but IDK lol
        ZonedDateTime time = ZonedDateTime.now()
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(1);

        return time.toInstant().toEpochMilli();
    }

    public void addListener(DayChangeListener runnable){
        listeners.add(runnable);
    }
}