package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.dungeons.rewrite_4.CompTracker;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;

public abstract class SpawnHandler extends BossComponent {
    protected CompTracker<BossStatusListener> statusListeners;

    @Override
    protected void onBossSet(DungeonBoss boss) {
        statusListeners = CompTracker.bossStatusFamily();
        boss.addTracker(statusListeners);
    }

    public CompTracker<BossStatusListener> getStatusListeners() {
        return statusListeners;
    }

    protected void callSpawnListeners(BossContext context) {
        for (BossStatusListener c: statusListeners) {
            c.onBossSummon(context);
        }
    }

    public abstract void onSpawn(BossContext context);

    public static class SimpleHandler extends SpawnHandler {
        @Override
        public void onSpawn(BossContext context) {
            callSpawnListeners(context);
        }
    }
}