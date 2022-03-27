package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.dungeons.rewrite_4.CompTracker;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TickController extends BossComponent implements BossStatusListener, Runnable {
    private CompTracker<TickComponent> tickComponents;
    private long bossTick;
    private BukkitTask task;

    @Override
    protected void onBossSet(DungeonBoss boss) {
        tickComponents = new CompTracker<>(TickComponent.class);
        boss.addTracker(tickComponents);
    }

    @Override
    public void onBossDeath(BossContext context, boolean forced) {
        task.cancel();
        task = null;
        bossTick = 0L;
    }

    @Override
    public void onBossSummon(BossContext context) {
        bossTick = 0L;
        task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this, 1, 1);
    }

    @Override
    public void run() {
        bossTick++;

        for (TickComponent c: tickComponents) {
            c.tick(bossTick);
        }
    }
}