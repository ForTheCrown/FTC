package net.forthecrown.crownevents.engine;

import net.forthecrown.core.Crown;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ArenaTickUpdater implements Runnable {
    protected final int updateInterval;
    protected BukkitTask task;
    EventArena arena;

    public ArenaTickUpdater(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void start() {
        Validate.isTrue(task == null || task.isCancelled(), "Task is already running");

        task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this, updateInterval, updateInterval);
    }

    public void stop() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
    }

    @Override
    public void run() {
        arena.onUpdate();
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public BukkitTask getTask() {
        return task;
    }
}
