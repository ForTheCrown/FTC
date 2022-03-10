package net.forthecrown.dungeons.level;

import net.forthecrown.core.Crown;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class SpawnerView {
    static final Var<Integer> VIEW_TIMEOUT = Var.def("dungeons_spawnerView_timeout", VarTypes.INT, 30 * 20); // def: 30 secs

    private final Player viewer;
    private final Slime[] slimes;
    private BukkitTask task;

    public SpawnerView(Player viewer, Slime[] slimes) {
        this.viewer = viewer;
        this.slimes = slimes;
    }

    void startTask(DungeonLevelImpl level) {
        stopTask();

        task = Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            level.stopViewing(this);
        }, VIEW_TIMEOUT.get());
    }

    void stopTask() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
    }

    public Player getViewer() {
        return viewer;
    }

    public Slime[] getSlimes() {
        return slimes;
    }
}
