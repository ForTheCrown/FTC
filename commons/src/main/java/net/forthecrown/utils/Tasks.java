package net.forthecrown.utils;

import static net.forthecrown.utils.PluginUtil.getCallingPlugin;
import static net.kyori.adventure.util.Ticks.SINGLE_TICK_DURATION_MS;
import static org.bukkit.Bukkit.getScheduler;

import java.time.Duration;
import java.util.function.Consumer;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class full of delegation methods for the {@link BukkitScheduler} instance.
 */
public final class Tasks {
  private Tasks() {}

  /**
   * Runs a task in sync with the server
   *
   * @param task The task to run
   * @return The created bukkit task
   */
  public static BukkitTask runSync(Runnable task) {
    return getScheduler().runTask(getCallingPlugin(), task);
  }

  /**
   * Runs a given task in sync after a tick delay
   *
   * @param task  The task to run
   * @param delay The amount of ticks to delay execution by
   * @return The created task
   */
  public static BukkitTask runLater(Runnable task, long delay) {
    return getScheduler().runTaskLater(getCallingPlugin(), task, delay);
  }

  /**
   * Runs a given task in sync after a tick delay
   *
   * @param task  The task to run
   * @param delay The amount of ticks to delay execution by
   * @return The created task
   */
  public static BukkitTask runLater(Runnable task, Duration delay) {
    return getScheduler().runTaskLater(getCallingPlugin(), task, toTicks(delay));
  }

  /**
   * Runs a sync task in regular intervals
   *
   * @param runnable The task to run
   * @param delay    The initial execution delay
   * @param interval The tick interval at which to run the task
   * @return The created task
   */
  public static BukkitTask runTimer(Runnable runnable, long delay, long interval) {
    return getScheduler().runTaskTimer(getCallingPlugin(), runnable, delay, interval);
  }

  /**
   * Runs a sync task in regular intervals
   *
   * @param runnable The task to run
   * @param delay    The initial execution delay
   * @param interval The tick interval at which to run the task
   * @return The created task
   */
  public static BukkitTask runTimer(Runnable runnable, Duration delay, Duration interval) {
    return getScheduler()
        .runTaskTimer(getCallingPlugin(), runnable, toTicks(delay), toTicks(interval));
  }

  /**
   * Runs a sync task in regular intervals
   *
   * @param runnable The task to run
   * @param delay    The initial execution delay
   * @param interval The tick interval at which to run the task
   */
  public static void runTimer(Consumer<BukkitTask> runnable, long delay, long interval) {
    getScheduler().runTaskTimer(getCallingPlugin(), runnable, delay, interval);
  }

  /**
   * Runs a sync task in regular intervals
   *
   * @param runnable The task to run
   * @param delay    The initial execution delay
   * @param interval The tick interval at which to run the task
   */
  public static void runTimer(Consumer<BukkitTask> runnable, Duration delay, Duration interval) {
    getScheduler().runTaskTimer(getCallingPlugin(), runnable, toTicks(delay), toTicks(interval));
  }

  /**
   * Runs a given task asynchronously
   *
   * @param runnable The task to run
   * @return The created task
   */
  public static BukkitTask runAsync(Runnable runnable) {
    return getScheduler().runTaskAsynchronously(getCallingPlugin(), runnable);
  }

  /**
   * Runs a given task asynchronously after a delay
   *
   * @param task  The task to run
   * @param delay The amount of ticks to delay execution by
   * @return The created task
   */
  public static BukkitTask runLaterAsync(Runnable task, long delay) {
    return getScheduler().runTaskLaterAsynchronously(getCallingPlugin(), task, delay);
  }

  /**
   * Runs a given task asynchronously after a delay
   *
   * @param task  The task to run
   * @param delay The amount of ticks to delay execution by
   * @return The created task
   */
  public static BukkitTask runLaterAsync(Runnable task, Duration delay) {
    return getScheduler().runTaskLaterAsynchronously(getCallingPlugin(), task, toTicks(delay));
  }

  /**
   * Runs a task asynchronously at regular intervals
   *
   * @param task     The task to run
   * @param delay    The initial tick delay
   * @param interval The tick interval between executions
   * @return The created task
   */
  public static BukkitTask runTimerAsync(Runnable task, long delay, long interval) {
    return getScheduler().runTaskTimerAsynchronously(getCallingPlugin(), task, delay, interval);
  }

  /**
   * Runs a task asynchronously at regular intervals
   *
   * @param task     The task to run
   * @param delay    The initial tick delay
   * @param interval The tick interval between executions
   * @return The created task
   */
  public static BukkitTask runTimerAsync(Runnable task, Duration delay, Duration interval) {
    return getScheduler()
        .runTaskTimerAsynchronously(getCallingPlugin(), task, toTicks(delay), toTicks(interval));
  }

  /**
   * Runs a task asynchronously at regular intervals
   *
   * @param task     The task to run
   * @param delay    The initial tick delay
   * @param interval The tick interval between executions
   */
  public static void runTimerAsync(Consumer<BukkitTask> task, long delay, long interval) {
    getScheduler().runTaskTimerAsynchronously(getCallingPlugin(), task, delay, interval);
  }


  /**
   * Runs a task asynchronously at regular intervals
   *
   * @param task     The task to run
   * @param delay    The initial tick delay
   * @param interval The tick interval between executions
   */
  public static void runTimerAsync(Consumer<BukkitTask> task, Duration delay, Duration interval) {
    getScheduler()
        .runTaskTimerAsynchronously(getCallingPlugin(), task, toTicks(delay), toTicks(interval));
  }

  /**
   * Checks if the given task is scheduled
   *
   * @param task The task to check
   * @return True, if the given task is not null and not cancelled.
   */
  public static boolean isScheduled(BukkitTask task) {
    return task != null && !task.isCancelled();
  }

  /**
   * Ensures the given task is cancelled or null, if not, it cancels the task and returns null.
   *
   * @param task The task to cancel
   * @return Null
   */
  public static BukkitTask cancel(@Nullable BukkitTask task) {
    if (isScheduled(task)) {
      task.cancel();
    }
    return null;
  }

  private static long toTicks(Duration dur) {
    long millis = dur.toMillis();
    return millis / SINGLE_TICK_DURATION_MS;
  }
}