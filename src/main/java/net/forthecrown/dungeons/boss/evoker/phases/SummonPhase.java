package net.forthecrown.dungeons.boss.evoker.phases;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.math.vector.Vector3d;

public class SummonPhase implements AttackPhase {

  public static final double[][] SPAWNS = {
      // x, y, z, randomRange
      {-277.5 + 202,   34.0 - 48,   58.5 - 49,   1.1},
      {-286.5 + 202,   35.0 - 48,   55.5 - 49,   2.1},
      {-291.5 + 202,   34.0 - 48,   53.5 - 49,   1.1},
      {-288.5 + 202,   34.5 - 48,   46.5 - 49,   2.1},
      {-290.5 + 202,   34.0 - 48,   40.5 - 49,   0.6},
      {-286.5 + 202,   33.0 - 48,   36.5 - 49,     0},
      {-282.5 + 202,   33.0 - 48,   40.5 - 49,   0.6},
      {-272.5 + 202,   34.0 - 48,   30.5 - 49,   1.1},
      {-267.5 + 202,   34.5 - 48,   34.5 - 49,   2.1},
      {-262.5 + 202,   34.0 - 48,   45.0 - 49,   1.1},
      {-264.5 + 202,   34.0 - 48,   51.5 - 49,   0.6},
      {-272.5 + 202,   33.1 - 48,   50.5 - 49,   1.1},
      {-272.5 + 202,   33.0 - 48,   37.5 - 49,     0},
      {-265.5 + 202,   34.0 - 48,   59.5 - 49,     0},
      {-289.5 + 202,   34.0 - 48,   32.5 - 49,   1.1},
      {-280.5 + 202,   33.0 - 48,   50.5 - 49,     0}
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

    double xOffset = randomRange == 0
        ? 0
        : Util.RANDOM.nextDouble(-randomRange + 0.1D, randomRange);

    double zOffset = randomRange == 0
        ? 0
        : Util.RANDOM.nextDouble(-randomRange + 0.1D, randomRange);

    spawning = currentIndex < SPAWNS.length;

    Vector3d pos = new Vector3d(
        rawPos[0] + xOffset,
        rawPos[1],
        rawPos[2] + zOffset
    );

    Entity e = spawner.create(pos, boss.getWorld(), context);
    spawned.add(e);
    TOTAL_SPAWNED.add(e);

    if (e instanceof LivingEntity liv) {
      liv.setRemoveWhenFarAway(false);
    }

    EvokerEffects.summoningEffect(boss.getWorld(), Vectors.doubleFrom(e.getLocation()),
        e.getHeight(), e.getWidth());
  }

  public static void killAllSpawned() {
    TOTAL_SPAWNED.removeIf(Entity::isDead);

    for (Entity e : TOTAL_SPAWNED) {
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
    if (spawning) {
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