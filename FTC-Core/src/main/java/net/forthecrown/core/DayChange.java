package net.forthecrown.core;

import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * DayUpdate listens to a change in the day, by using the
 * server's restart time to change day stuff.
 */
public class DayChange {
    private final List<DayChangeListener> listeners = new ArrayList<>();

    private BukkitTask updateTask;

    DayChange() {}

    private void changeDay() {
        Crown.logger().info("Updating date");

        listeners.forEach(r -> {
            try {
                r.onDayChange();
            } catch (Exception e){
                Crown.logger().error("Could not update date of " + r.getClass().getSimpleName());
                e.printStackTrace();
            }
        });
    }

    void schedule() {
        // Cancel if previous task exists
        if(updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }

        // Configure calendar to be at the start of the next day
        // So we can run day update exactly on time. As always,
        // there's probably a better way of doing this, but IDK lol
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);

        long tomorrow = calendar.getTimeInMillis();

        // Find difference between now and tomorrow
        long difference = TimeUtil.timeUntil(tomorrow);

        Crown.logger().info("DayUpdate scheduled, executing in: " + new TimePrinter(difference).printString());

        // Convert to ticks for bukkit scheduler
        difference = TimeUtil.millisToTicks(difference);

        // Run update on next day change and then run it every
        // 24 hours, aka once a day. It probably won't get ran
        // a second time cuz of daily restart, but whatever lol
        // future-proof :D
        updateTask = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this::changeDay, difference, TimeUtil.millisToTicks(TimeUtil.DAY_IN_MILLIS));
    }

    public void addListener(DayChangeListener runnable){
        listeners.add(runnable);
    }

    public List<DayChangeListener> getListeners() {
        return listeners;
    }
}
