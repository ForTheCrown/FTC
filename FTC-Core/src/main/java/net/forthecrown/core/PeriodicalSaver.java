package net.forthecrown.core;

import net.forthecrown.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

/**
 * Class which saves the FTC-Core in the interval given in the autoSaveIntervalMins comvar
 */
final class PeriodicalSaver {
    private BukkitTask task;

    PeriodicalSaver() {
        FtcVars.autoSaveIntervalMins.setUpdateListener(aLong -> start());
    }

    public void start() {
        cancel();
        long interval = TimeUnit.MINUTES.toMillis(FtcVars.autoSaveIntervalMins.getValue(60L));
        interval = TimeUtil.millisToTicks(interval);

        task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), Crown::saveFTC, interval, interval);
    }

    public void cancel() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
    }

    public boolean isScheduled() {
        return task != null && !task.isCancelled();
    }
}
