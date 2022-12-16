package net.forthecrown.dungeons.boss.evoker;

import net.forthecrown.utils.TickSequence;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class EvokerSequences {
    public static final BossMessage
            AWAKEN          = BossMessage.simple("summon_awaken"),
            INSULT          = BossMessage.partySize("summon_insult"),
            END_MESSAGE     = BossMessage.simple("summon_shield"),
            END_MESSAGE_L2  = BossMessage.simple("summon_shield_secondline"),
            DEATH_START     = BossMessage.simple("death_start"),
            DEATH_MIDDLE    = BossMessage.partySize("death_middle"),
            DEATH_END       = BossMessage.simple("death_end");

    public static final BossMessage[] SUMMON_AUTO_MSGS = {
        AWAKEN, INSULT
    };

    public static TickSequence createSummoning(EvokerBoss boss) {
        TickSequence result = new TickSequence();
        final int messageInterval = 20;
        final int messageTicks = (SUMMON_AUTO_MSGS.length * messageInterval) + messageInterval;

        result.addNode(() -> {
            boss.setInvulnerable(false);
            boss.getRoom().getPlayers().forEach(player -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, messageTicks, 1, false, true));
            });
        }, 0);

        for (BossMessage m: SUMMON_AUTO_MSGS) {
            result.addNode(() -> boss.broadcast(false, m), messageInterval);
        }

        result.addNode(boss::onSummoningFinish, 15);
        result.addNode(() -> {
            boss.broadcast(false, END_MESSAGE, END_MESSAGE_L2);
            boss.getBossEntity().setGlowing(true);
            boss.setInvulnerable(true);
        }, messageInterval);
        result.addNode(() -> boss.nextPhase(false), messageInterval);

        return result;
    }

    public static TickSequence createDeath(EvokerBoss boss) {
        TickSequence result = new TickSequence();
        final int interval = EvokerConfig.deathAnimLength / 2;

        result.addNode(() -> boss.broadcast(false, DEATH_START), 0);
        result.addNode(() -> boss.broadcast(false, DEATH_MIDDLE), interval);

        result.addNode(() -> {
            boss.broadcast(false, DEATH_END);
            boss.kill(false);
        }, interval);

        return result;
    }
}