package net.forthecrown.utils;

import net.forthecrown.core.Crown;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Utility class full of delegation methods
 * for the {@link BukkitScheduler} instance.
 */
public final class Tasks {
    private Tasks() {}

    /**
     * Runs a task in sync with the server
     * @param task The task to run
     * @return The created bukkit task
     */
    public static BukkitTask runSync(Runnable task) {
        return scheduler().runTask(Crown.plugin(), task);
    }

    /**
     * Runs a given task in sync after a tick delay
     * @param task The task to run
     * @param delay The amount of ticks to delay execution by
     * @return The created task
     */
    public static BukkitTask runLater(Runnable task, long delay) {
        return scheduler().runTaskLater(Crown.plugin(), task, delay);
    }

    /**
     * Runs a sync task in regular intervals
     * @param runnable The task to run
     * @param delay The initial execution delay
     * @param interval The tick interval at which to run the task
     * @return The created task
     */
    public static BukkitTask runTimer(Runnable runnable, long delay, long interval) {
        return scheduler().runTaskTimer(Crown.plugin(), runnable, delay, interval);
    }

    /**
     * Runs a sync task in regular intervals
     * @param runnable The task to run
     * @param delay The initial execution delay
     * @param interval The tick interval at which to run the task
     */
    public static void runTimer(Consumer<BukkitTask> runnable, long delay, long interval) {
        scheduler().runTaskTimer(Crown.plugin(), runnable, delay, interval);
    }

    /**
     * Runs a given task asynchronously
     * @param runnable The task to run
     * @return The created task
     */
    public static BukkitTask runAsync(Runnable runnable) {
        return scheduler().runTaskAsynchronously(Crown.plugin(), runnable);
    }

    /**
     * Runs a given task asynchronously after a delay
     * @param task The task to run
     * @param delay The amount of ticks to delay execution by
     * @return The created task
     */
    public static BukkitTask runLaterAsync(Runnable task, long delay) {
        return scheduler().runTaskLaterAsynchronously(Crown.plugin(), task, delay);
    }

    /**
     * Runs a task asynchronously at regular intervals
     * @param task The task to run
     * @param delay The initial tick delay
     * @param interval The tick interval between executions
     * @return The created task
     */
    public static BukkitTask runTimerAsync(Runnable task, long delay, long interval) {
        return scheduler().runTaskTimerAsynchronously(Crown.plugin(), task, delay, interval);
    }

    /**
     * Runs a task asynchronously at regular intervals
     * @param task The task to run
     * @param delay The initial tick delay
     * @param interval The tick interval between executions
     */
    public static void runTimerAsync(Consumer<BukkitTask> task, long delay, long interval) {
        scheduler().runTaskTimerAsynchronously(Crown.plugin(), task, delay, interval);
    }

    /**
     * Checks if the given task is scheduled
     * @param task The task to check
     * @return True, if the given task is not null and not cancelled.
     */
    public static boolean isScheduled(BukkitTask task) {
        return task != null && !task.isCancelled();
    }

    /**
     * Ensures the given task is cancelled or null,
     * if not, it cancels the task and returns null.
     * @param task The task to cancel
     * @return Null
     */
    public static BukkitTask cancel(@Nullable BukkitTask task) {
        if (!isScheduled(task)) {
            return null;
        }

        task.cancel();
        return null;
    }

    public static BukkitScheduler scheduler() {
        return Bukkit.getScheduler();
    }
}