package net.forthecrown.dungeons.boss.evoker.phases;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.components.MinionSpawnerComponent;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.vector.Vector3d;

/**
 * The phase where he starts attacking with his own magic, kinda weak ngl lol
 */
public class SwarmPhase implements AttackPhase {
  EvokerBoss boss;

  int spawned = 0;
  int tick = -1;
  int requiredSpawns = 0;

  private static final List<Vex> spawnedVexes = new ObjectArrayList<>();

  public static void killAllSpawned() {
    spawnedVexes.forEach(MinionSpawnerComponent::kill);
    spawnedVexes.clear();
  }

  @Override
  public void onStart(EvokerBoss boss, BossContext context) {
    reset();
    this.boss = boss;
    requiredSpawns = context.players().size() + 2;
    boss.getPhaseBar().setTitle("Spawning vexes! (" + requiredSpawns + ")");
    boss.getPhaseBar().setVisible(true);
  }

  @Override
  public void onEnd(EvokerBoss boss, BossContext context) {
    reset();
    this.boss = null;
  }

  @Override
  public void onTick(EvokerBoss boss, BossContext context) {
    double progress;
    ++tick;

    if (spawned < requiredSpawns) {
      progress = (double) tick / EvokerConfig.swarm_summonInterval;

      if (tick >= EvokerConfig.swarm_summonInterval) {
        tick = 0;
        summon();
      }
    } else {
      progress = (double) tick / EvokerConfig.swarm_endingDelay;
    }

    boss.getPhaseBar().setProgress(progress);

    if (spawned < requiredSpawns
        || tick < EvokerConfig.swarm_endingDelay
    ) {
      return;
    }

    boss.nextPhase(true);
  }

  void reset() {
    this.tick = -1;
    this.spawned = 0;
    this.requiredSpawns = 0;
  }

  void summon() {
    ++spawned;
    spawnVexes(
        boss.getBossEntity(),
        boss.currentContext(),
        PotionPhase.findTarget(boss)
    );

    if (spawned >= requiredSpawns) {
      boss.getPhaseBar().setTitle("Defeat the vexes!");
    } else {
      boss.getPhaseBar().setTitle(
          "Spawning vexes! (" + (requiredSpawns - spawned) + ")"
      );
    }

    boss.getBossEntity()
        .getWorld()
        .strikeLightningEffect(boss.getBossEntity().getLocation());
  }

  void spawnVexes(Evoker boss, BossContext context, Player target) {
    double dist = EvokerConfig.swarm_vexSpawnDist;
    int amount = EvokerConfig.swarm_vexAmount;

    double circleSize = EvokerConfig.swarm_spawnRadial;
    double degreeInterval = circleSize / amount;
    double radianInterval = Math.toRadians(degreeInterval);

    if (target != null) {
      boss.lookAt(target);
    }

    // Center of the entity
    Location loc = boss.getLocation();
    loc = boss.getBoundingBox().getCenter()
        .toLocation(loc.getWorld(), loc.getYaw(), loc.getPitch());

    // Direction the boss is facing
    Vector3d dir = Vectors.doubleFrom(loc.getDirection())
        .normalize()
        .mul(dist)
        .withY(0);

    Vector3d pos = Vectors.doubleFrom(loc);

    Quaterniond initialRotation
        = Quaterniond.fromAxesAnglesDeg(-90, 0, degreeInterval);

    Vector3d left = initialRotation.rotate(dir);
    Quaterniond rotation = Quaterniond.fromAxesAnglesRad(0, 0, -radianInterval);

    for (int i = 0; i < amount; i++) {
      left = rotation.rotate(left);
      Vector3d spawn = left.add(pos);
      Location l = new Location(
          boss.getWorld(),
          spawn.x(), spawn.y(), spawn.z(),
          loc.getYaw(), loc.getPitch()
      );

      l.getWorld().spawn(l, Vex.class, vex -> {
        AttributeInstance maxHealth
            = vex.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        Util.clearModifiers(maxHealth);
        maxHealth.setBaseValue(1D);
        maxHealth.addModifier(context.healthModifier());

        double health = maxHealth.getValue();
        vex.setHealth(health);

        if (target != null) {
          vex.lookAt(target);
          vex.setTarget(target);
        }

        spawnedVexes.add(vex);
      });
    }
  }
}