package net.forthecrown.dungeons.boss.evoker.phases;

import java.util.LinkedList;
import java.util.List;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ghast;

public class GhastPhase implements AttackPhase {
  public static final byte EXPLOSION_POWER = 3;

  public static final double[][] SPAWNS = {
      {-277.5 + 202, 37 - 48, 38.5 - 49},
      {-277.5 + 202, 37 - 48, 50.5 - 49}
  };

  static final List<Ghast> SPAWNED = new LinkedList<>();

  static final BossMessage START = BossMessage.simple("phase_ghast_start");

  private int tick;

  public static void killAllSpawned() {
    SPAWNED.removeIf(org.bukkit.entity.Entity::isDead);
    SPAWNED.forEach(org.bukkit.entity.Entity::remove);
    SPAWNED.clear();
  }

  @Override
  public void onStart(EvokerBoss boss, BossContext context) {
    tick = 0;
    boss.getPhaseBar().setVisible(true);
    boss.getPhaseBar().setTitle("Ghasts, deflect their attacks!");

    boss.broadcast(false, START);

    for (double[] pos : SPAWNS) {
      Location l = new Location(boss.getWorld(), pos[0], pos[1], pos[2]);

      boss.getWorld().spawn(l, Ghast.class, ghast -> {
        Entity nms = VanillaAccess.getEntity(ghast);

        // explosionPower cannot be changed by setters, Bukkit doesn't
        // change this, so instead of using reflection, I just save
        // the ghast into NBT, modify the NBT, and then load the ghast
        // from that same NBT
        CompoundTag saved = new CompoundTag();
        nms.save(saved);

        saved.putByte("ExplosionPower", EXPLOSION_POWER);
        nms.load(saved);

        AttackPhases.clearAllDrops(ghast);

        double health = EvokerConfig.ghast_health;
        AttributeInstance maxHealth = ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        DungeonUtils.clearModifiers(maxHealth);

        maxHealth.setBaseValue(health);
        ghast.setHealth(health);
        ghast.setRemoveWhenFarAway(false);

        SPAWNED.add(ghast);
      });
    }
  }

  @Override
  public void onEnd(EvokerBoss boss, BossContext context) {

  }

  @Override
  public void onTick(EvokerBoss boss, BossContext context) {
    tick++;

    if (tick >= EvokerConfig.ghast_length) {
      boss.nextPhase(false);
    } else {
      double progress = (double) tick / (double) EvokerConfig.ghast_length;
      boss.getPhaseBar().setProgress(progress);
    }
  }
}