package net.forthecrown.core.module;

import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import org.bukkit.scheduler.BukkitTask;

/**
 * Class which saves the FTC-Core in the interval given in the autoSaveIntervalMins comvar
 */
public final class AutoSave extends ModuleService {
    private BukkitTask task;

    AutoSave() {
        super(OnSave.class);
    }

    public void schedule() {
        cancel();

        long interval = GeneralConfig.autoSaveInterval;
        interval = Time.millisToTicks(interval);

        task = Tasks.runTimer(this, interval, interval);
    }

    public void cancel() {
        task = Tasks.cancel(task);
    }
}