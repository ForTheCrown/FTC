package net.forthecrown.dungeons.boss.evoker;

import net.forthecrown.utils.TickSequence;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class EvokerSequences {

  public static final BossMessage
      AWAKEN = BossMessage.simple("summon_awaken"),
      INSULT = BossMessage.partySize("summon_insult"),
      SHIELD = BossMessage.simple("summon_shield"),
      SHIELD_SECONDLINE = BossMessage.simple("summon_shield_secondline"),
      LETS_BATTLE = BossMessage.simple("summon_lets_go"),

      DEATH_START = BossMessage.simple("death_start"),
      DEATH_MIDDLE = BossMessage.partySize("death_middle"),
      DEATH_END = BossMessage.simple("death_end");

  public static final PotionEffect BLINDNESS
      = new PotionEffect(PotionEffectType.BLINDNESS, 1000, 0);

  public static TickSequence createSummoning(EvokerBoss boss) {
    TickSequence result = new TickSequence();

    result.addNode(() -> {
      boss.setInvulnerable(false);
      boss.getRoom().getPlayers().forEach(player -> {
        player.addPotionEffect(BLINDNESS);
      });
    }, 0);

    result.addNode(() -> boss.broadcast(false, AWAKEN), 40);

    result.addNode(() -> {
      boss.broadcast(false, INSULT);
      boss.onSummoningFinish();
    }, 20);

    result.addNode(() -> {
      boss.getRoom().getPlayers().forEach(player -> {
        player.removePotionEffect(BLINDNESS.getType());
      });
    }, 40);

    result.addNode(() -> {
      boss.broadcast(false, SHIELD);
    }, 40);

    result.addNode(() -> {
      boss.broadcast(false, SHIELD_SECONDLINE);
      boss.getBossEntity().setGlowing(true);
      boss.setInvulnerable(true);
    }, 20);

    result.addNode(() -> {
      boss.broadcast(false, LETS_BATTLE);
      boss.nextPhase(false);
    }, 40);

    return result;
  }

  public static TickSequence createDeath(EvokerBoss boss) {
    TickSequence result = new TickSequence();

    result.addNode(() -> boss.broadcast(false, DEATH_START), 0);
    result.addNode(() -> boss.broadcast(false, DEATH_MIDDLE), 40);
    result.addNode(() -> boss.broadcast(false, DEATH_END), 20);

    result.addNode(() -> {
      EvokerEffects.lightning(boss);
      boss.kill(false);
    }, 40);

    return result;
  }
}