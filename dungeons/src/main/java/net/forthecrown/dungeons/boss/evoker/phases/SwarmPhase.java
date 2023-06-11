package net.forthecrown.dungeons.boss.evoker.phases;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.components.MinionSpawnerComponent;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.dungeons.boss.evoker.EvokerEffects;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.loot.LootTables;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.vector.Vector3d;

/**
 * The phase where the evoker spawns a swarm of weak vexes
 */
public class SwarmPhase implements AttackPhase {
  public static final BossMessage
      BEGIN = BossMessage.simple("phase_swarm_begin"),
      END = BossMessage.simple("phase_swarm_end");

  EvokerBoss boss;

  int spawned = 0;
  int tick = -1;
  int requiredWaves = 0;

  private static final List<Vex> spawnedVexes = new ObjectArrayList<>();

  public static void killAllSpawned() {
    spawnedVexes.forEach(MinionSpawnerComponent::kill);
    spawnedVexes.clear();
  }

  @Override
  public void onStart(EvokerBoss boss, BossContext context) {
    reset();
    this.boss = boss;

    // Phase should spawn a minimum of 3 waves, with +1 extra
    // wave per each player
    requiredWaves = context.players().size() + 2;

    boss.getPhaseBar().setTitle("Spawning vexes! (" + requiredWaves + ")");
    boss.getPhaseBar().setVisible(true);

    boss.broadcast(true, BEGIN);
  }

  @Override
  public void onEnd(EvokerBoss boss, BossContext context) {
    reset();
    this.boss = null;
    boss.broadcast(true, END);
  }

  @Override
  public void onTick(EvokerBoss boss, BossContext context) {
    double progress;
    ++tick;

    // If more waves should be spawned
    if (spawned < requiredWaves) {
      progress = (double) tick / EvokerConfig.swarm_summonInterval;

      // If should spawn
      if (tick >= EvokerConfig.swarm_summonInterval) {
        tick = 0;
        summon();
      }
    } else {
      progress = (double) tick / EvokerConfig.swarm_endingDelay;
    }

    boss.getPhaseBar().setProgress(progress);

    if (spawned < requiredWaves
        || tick < EvokerConfig.swarm_endingDelay
    ) {
      return;
    }

    boss.nextPhase(false);
  }

  /** Reset spawn and tick counters */
  void reset() {
    this.tick = -1;
    this.spawned = 0;
    this.requiredWaves = 0;
  }

  /** Make the boss summon a wave of vexes */
  void summon() {
    ++spawned;
    spawnVexes(
        boss.getBossEntity(),
        boss.currentContext(),
        PotionPhase.findTarget(boss)
    );

    // Adjust boss bar to display how many more vex waves
    // will be spawned, or to show "Defeat the vexes!"
    if (spawned >= requiredWaves) {
      boss.getPhaseBar().setTitle("Defeat the vexes!");
    } else {
      boss.getPhaseBar().setTitle(
          "Spawning vexes! (" + (requiredWaves - spawned) + ")"
      );
    }

    // Cool lightning strike when summoning
    EvokerEffects.lightning(boss);
  }

  /** Spawns vexes in a circle around the boss */
  void spawnVexes(Evoker boss, BossContext context, Player target) {
    double dist = EvokerConfig.swarm_vexSpawnDist;
    int amount = EvokerConfig.swarm_vexAmount;

    // The size of the circle in which the
    // vexes spawn, in degrees
    double circleSize = EvokerConfig.swarm_spawnRadial;

    // Radial interval between each vex's spawn position
    double degreeInterval = circleSize / amount;
    double radianInterval = Math.toRadians(degreeInterval);

    if (target != null) {
      boss.lookAt(target);
    }

    // Get center of the entity
    Location loc = boss.getLocation();
    loc = boss.getBoundingBox().getCenter()
        .toLocation(loc.getWorld(), loc.getYaw(), loc.getPitch());

    // Direction the boss is facing, normalized to dist
    Vector3d dir = Vectors.doubleFrom(loc.getDirection())
        .normalize()
        .mul(dist)
        .withY(0);

    Vector3d pos = Vectors.doubleFrom(loc);

    Quaterniond initialRotation
        = Quaterniond.fromAxesAnglesDeg(-90, 0, degreeInterval);
    Quaterniond rotation = Quaterniond.fromAxesAnglesRad(0, 0, -radianInterval);

    Vector3d left = initialRotation.rotate(dir);

    for (int i = 0; i < amount; i++) {
      // Rotate position
      left = rotation.rotate(left);

      // Get spawn location
      Vector3d spawn = left.add(pos);
      Location l = new Location(
          boss.getWorld(),
          spawn.x(), spawn.y(), spawn.z(),
          loc.getYaw(), loc.getPitch()
      );

      // Spawn vex
      l.getWorld().spawn(l, Vex.class, vex -> {
        AttributeInstance maxHealth
            = vex.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        assert maxHealth != null;

        DungeonUtils.clearModifiers(maxHealth);
        maxHealth.setBaseValue(1D);
        maxHealth.addModifier(context.healthModifier());

        double health = maxHealth.getValue();
        vex.setHealth(health);
        vex.setRemoveWhenFarAway(false);

        vex.setLootTable(LootTables.EMPTY.getLootTable());

        if (target != null) {
          vex.lookAt(target);
          vex.setTarget(target);
        }

        spawnedVexes.add(vex);
      });
    }
  }
}