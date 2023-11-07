package net.forthecrown.utils;

import java.time.Duration;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class PeriodicalSaver {

  private final Runnable callback;

  private final Plugin plugin;

  private final LongSupplier delayGetter;

  private BukkitTask task;

  private PeriodicalSaver(Runnable callback, Plugin plugin, LongSupplier delayGetter) {
    this.callback = callback;
    this.plugin = plugin;
    this.delayGetter = delayGetter;
  }

  public static PeriodicalSaver create(Runnable runnable, Supplier<Duration> delayGetter) {
    Plugin plugin = PluginUtil.getCallingPlugin();
    return new PeriodicalSaver(runnable, plugin, () -> delayGetter.get().toMillis());
  }

  public static PeriodicalSaver create(Runnable runnable, LongSupplier delayGetter) {
    Plugin plugin = PluginUtil.getCallingPlugin();
    return new PeriodicalSaver(runnable, plugin, delayGetter);
  }

  public void start() {
    if (Tasks.isScheduled(task)) {
      stop();
    }

    long interval = delayGetter.getAsLong();
    task = Bukkit.getScheduler().runTaskTimer(plugin, callback, interval, interval);
  }

  public void stop() {
    Tasks.cancel(task);
    task = null;
  }
}