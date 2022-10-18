package net.forthecrown.dungeons.boss.evoker.phases;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerVars;
import net.forthecrown.utils.Util;
import org.bukkit.entity.Spellcaster;

/**
 * The phase where he starts attacking with his own magic,
 * kinda weak ngl lol
 */
public class NormalAttackPhase implements AttackPhase {
    private int tick;

    public static final BossMessage
        START = BossMessage.random("phase_normal_start", 2),
        END = BossMessage.partySize("phase_normal_end");

    @Override
    public void onStart(EvokerBoss boss, BossContext context) {
        tick = 0;
        boss.setAttackingAllowed(true);
        boss.getPhaseBar().setVisible(true);
        boss.getPhaseBar().setTitle("Magic attacks, look out!");

        boss.broadcast(true, START);
    }

    @Override
    public void onEnd(EvokerBoss boss, BossContext context) {
        boss.setAttackingAllowed(false);
        boss.broadcast(true, END);
    }

    @Override
    public void onTick(EvokerBoss boss, BossContext context) {
        tick++;

        if (boss.getBossEntity().getSpell() == Spellcaster.Spell.NONE) {
            boss.getBossEntity().setSpell(Util.RANDOM.nextBoolean() ? Spellcaster.Spell.FANGS : Spellcaster.Spell.SUMMON_VEX);
        }

        if (tick >= EvokerVars.normal_length) {
            boss.nextPhase(true);
        } else {
            double progress = (double) tick / (double) EvokerVars.normal_length;
            boss.getPhaseBar().setProgress(progress);
        }
    }
}