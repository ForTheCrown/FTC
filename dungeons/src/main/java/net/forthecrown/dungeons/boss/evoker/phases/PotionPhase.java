package net.forthecrown.dungeons.boss.evoker.phases;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.dungeons.boss.evoker.EvokerEffects;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector3d;

public class PotionPhase implements AttackPhase {

  private static final List<QueuedPotion> QUEUED_POTIONS
      = new ObjectArrayList<>();

  static final BossMessage
      START_MESSAGE = BossMessage.simple("phase_potion_start");

  private int tick = 0;
  private int potionTick;

  @Override
  public void onStart(EvokerBoss boss, BossContext context) {
    BossBar bar = boss.getPhaseBar();
    bar.setVisible(true);
    bar.setTitle("Potions dropping, look out!");

    // Set the spell so he waves his arms around
    // but don't allow attacking
    boss.setSpell(Spellcaster.Spell.SUMMON_VEX);
    boss.setAttackingAllowed(false);

    boss.broadcast(true, START_MESSAGE);
    tick = 0;
  }

  @Override
  public void onEnd(EvokerBoss boss, BossContext context) {
    QUEUED_POTIONS.clear();
    boss.setSpell(null);
  }

  @Override
  public void onTick(EvokerBoss boss, BossContext context) {
    tick++;
    potionTick++;

    double progress = (double) tick / (double) EvokerConfig.potion_length;
    boss.getPhaseBar().setProgress(progress);

    if (tick >= EvokerConfig.potion_length) {
      boss.nextPhase(true);
      return;
    }

    Random random = boss.getRandom();

    if (!QUEUED_POTIONS.isEmpty()) {
      Iterator<QueuedPotion> iterator = QUEUED_POTIONS.iterator();

      while (iterator.hasNext()) {
        QueuedPotion e = iterator.next();
        e.untilSpawn--;

        if (e.untilSpawn > 0) {
          continue;
        }

        Vector2d next = e.pos;
        Location l = new Location(
            boss.getWorld(),
            next.x(), EvokerConfig.potion_spawnY, next.y()
        );

        int duration = EvokerConfig.potion_length - tick;

        l.getWorld().spawn(l, ThrownPotion.class, potion -> {
          ItemStack item = ItemStacks.potionBuilder(Material.LINGERING_POTION, 1)
              .addEffect(new PotionEffect(
                  random.nextBoolean()
                      ? PotionEffectType.POISON
                      : PotionEffectType.WITHER,

                  duration, 1, false, true, true
              ))
              .build();

          potion.setItem(item);
        });

        iterator.remove();
      }
    }

    if (potionTick < EvokerConfig.potion_throwInterval) {
      return;
    }

    potionTick = 0;
    Vector2d potionSpawn;

    if (random.nextBoolean()) {
      Player p = findTarget(boss, random);

      if (p == null) {
        Loggers.getLogger().warn("Found no targets for PotionPhase");
        return;
      }

      Location l = p.getLocation();
      potionSpawn = Vector2d.from(l.getX(), l.getZ());
    } else {
      potionSpawn = Vector2d.from(generateCord(random), generateCord(random));
    }

    QUEUED_POTIONS.add(new QueuedPotion(potionSpawn));
    Vector3d pos = new Vector3d(
        potionSpawn.x(),
        EvokerConfig.potion_spawnY,
        potionSpawn.y()
    );

    EvokerEffects.summoningSound(boss.getWorld(), pos);
    EvokerEffects.drawImpact(boss.getWorld(), pos, 1);
  }

  private static double generateCord(Random random) {
    double result = random.nextDouble(
        EvokerConfig.potion_minDist,
        EvokerConfig.potion_maxDist + 1
    );

    return random.nextBoolean() ? result : -result;
  }

  static Player findTarget(EvokerBoss boss, Random random) {
    var list = boss.getRoom().getPlayers()
        .stream()
        .filter(player ->
            (player.getGameMode() == GameMode.SURVIVAL
                || player.getGameMode() == GameMode.ADVENTURE)
                && !player.isDead()
        )
        .toList();

    if (list.isEmpty()) {
      return null;
    }

    return list.get(random.nextInt(list.size()));
  }

  @RequiredArgsConstructor
  private static class QueuedPotion {
    private final Vector2d pos;
    private int untilSpawn = EvokerConfig.potion_spawnDelay;
  }
}