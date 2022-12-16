package net.forthecrown.dungeons.boss.evoker.phases;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.components.MinionSpawnerComponent;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.dungeons.boss.evoker.EvokerEffects;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

public class SummonPhase implements AttackPhase {
    public static final double[][] SPAWNS = {
            // x, y, z, randomRange
            { -277.5, 34.0, 58.5, 1.1 },
            { -286.5, 35.0, 55.5, 2.1 },
            { -291.5, 34.0, 53.5, 1.1 },
            { -288.5, 34.5, 46.5, 2.1 },
            { -290.5, 34.0, 40.5, 0.6 },
            { -286.5, 33.0, 36.5, 0   },
            { -282.5, 33.0, 40.5, 0.6 },
            { -272.5, 34.0, 30.5, 1.1 },
            { -267.5, 34.5, 34.5, 2.1 },
            { -262.5, 34.0, 45.0, 1.1 },
            { -264.5, 34.0, 51.5, 0.6 },
            { -272.5, 33.1, 50.5, 1.1 },
            { -272.5, 33.0, 37.5, 0   },
            { -265.5, 34.0, 59.5, 0   },
            { -289.5, 34.0, 32.5, 1.1 },
            { -280.5, 33.0, 50.5, 0   }
    };

    private final BossMessage start, end;
    private final MinionSpawnerComponent.MinionSpawner spawner;

    private static final List<Entity> TOTAL_SPAWNED = new ObjectArrayList<>();
    private final List<Entity> spawned = new ObjectArrayList<>();

    private boolean spawning;
    private int spawnTick;
    private int currentIndex = 0;

    public SummonPhase(MinionSpawnerComponent.MinionSpawner spawner,
                       BossMessage start, BossMessage end
    ) {
        this.spawner = spawner;
        this.start = start;
        this.end = end;
    }

    @Override
    public void onStart(EvokerBoss boss, BossContext context) {
        currentIndex = 0;
        spawned.clear();
        spawning = true;

        boss.getPhaseBar().setTitle("Summoning mobs, kill them!");
        boss.getPhaseBar().setVisible(true);

        if (start == null) {
            return;
        }

        boss.broadcast(true, start);
    }

    @Override
    public void onEnd(EvokerBoss boss, BossContext context) {
        if (end == null) {
            return;
        }

        boss.broadcast(true, end);
    }

    private void countSpawned() {
        spawned.removeIf(Entity::isDead);
        TOTAL_SPAWNED.removeIf(Entity::isDead);
    }

    @Override
    public void onTick(EvokerBoss boss, BossContext context) {
        countSpawned();
        double progress = (double) spawned.size() / (double) SPAWNS.length;
        boss.getPhaseBar().setProgress(progress);
        checkSpawned();

        if (!spawning) {
            return;
        }

        spawnTick++;
        if (spawnTick <= EvokerConfig.ticksBetweenSpawns) {
            return;
        }

        spawnTick = 0;

        double[] rawPos = SPAWNS[currentIndex++];
        double randomRange = rawPos[3];
        double xOffset = randomRange == 0 ? 0 : Util.RANDOM.nextDouble(-randomRange + 0.1D, randomRange);
        double zOffset = randomRange == 0 ? 0 : Util.RANDOM.nextDouble(-randomRange + 0.1D, randomRange);

        spawning = currentIndex < SPAWNS.length;

        Vector3d pos = new Vector3d(
                rawPos[0] + xOffset,
                rawPos[1],
                rawPos[2] + zOffset
        );

        Entity e = spawner.create(pos, boss.getWorld(), context);
        spawned.add(e);
        TOTAL_SPAWNED.add(e);

        EvokerEffects.summoningEffect(boss.getWorld(), Vectors.doubleFrom(e.getLocation()), e.getHeight(), e.getWidth());
    }

    public static void killAllSpawned() {
        TOTAL_SPAWNED.removeIf(Entity::isDead);

        for (Entity e: TOTAL_SPAWNED) {
            MinionSpawnerComponent.kill(e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
        if (!spawned.contains(e)) {
            return;
        }

        spawned.remove(e);
        TOTAL_SPAWNED.remove(e);

        checkSpawned();
    }

    void checkSpawned() {
        if(spawning) {
            return;
        }

        if (spawned.size() < SPAWNS.length / 4) {
            Bosses.EVOKER.nextPhase(false);
        }
    }

    public static void shuffleSpawns() {
        int length = SPAWNS.length;

        for (int i = 0; i < length; i++) {
            int newIndex = Util.RANDOM.nextInt(length);

            double[] current = SPAWNS[i];
            double[] newCords = SPAWNS[newIndex];

            SPAWNS[newIndex] = current;
            SPAWNS[i] = newCords;
        }
    }
}