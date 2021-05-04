package net.forthecrown.mayevent.arena;

import org.bukkit.boss.BarColor;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaUpdater extends BukkitRunnable {

    byte weaponMessageDelay = 35;
    short pickupSpawnDelay = 300;

    final static short staticPrepTime = 20*15;
    short prepTime = staticPrepTime;

    short updateMobs = staticUpdateMobs;
    static final short staticUpdateMobs = 100;

    short waveDelay = 0;
    static final short staticWaveDelay = 200;

    short wallSpawnDelay = staticWallSpawnDelay;
    static final short staticWallSpawnDelay = 2400;

    private final EventArena arena;
    public ArenaUpdater(EventArena arena){ this.arena = arena; }

    @Override
    public void run() {
        if(prepTime > 0){
            prepTime--;

            double progress = (double) prepTime / (double) staticPrepTime;
            arena.bossBar.setProgress(progress);

            if(arena.bossBar.getProgress() == 0) arena.nextWaveActual();
        }

        if(waveDelay > 0){
            waveDelay--;

            double progress = (double) waveDelay / (double) staticWaveDelay;

            arena.bossBar.setTitle((waveDelay/20+1) + " seconds until next wave");
            arena.bossBar.setProgress(progress);

            if(arena.bossBar.getProgress() == 0) arena.nextWaveActual();
            return;
        }

        updateMobs--;
        if(arena.wave() > 0 && updateMobs < 0){
            updateMobs = staticUpdateMobs;
            arena.currentMobAmount = (short) arena.box.getEntitiesByType(Mob.class).size();
            arena.box.getEntitiesByType(Mob.class).forEach(m -> m.setTarget(arena.entry.player()));
            arena.updateBossbar();

            if(arena.bossBar.getProgress() == 0) arena.nextWave();
        }

        wallSpawnDelay--;
        if(wallSpawnDelay < 0){
            wallSpawnDelay = staticWallSpawnDelay;
            arena.checkIfOnWalls();
        }

        pickupSpawnDelay--;
        if(pickupSpawnDelay < 0){
            arena.spawnNextPickup();
            pickupSpawnDelay = 300;
        }

        weaponMessageDelay--;
        if(weaponMessageDelay > 0) return;

        arena.entry.sendGunMessage();
        weaponMessageDelay = 35;
    }

    public void startWaveDelay(){
        arena.bossBar.setColor(BarColor.YELLOW);
        waveDelay = staticWaveDelay;
    }
}
