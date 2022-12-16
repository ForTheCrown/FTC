package net.forthecrown.dungeons.boss.evoker.phases;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.*;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Evoker;

public class ShieldPhase implements AttackPhase {

    public static final BossMessage
            SHIELD_LOSE     = BossMessage.simple("phase_shield_lost"),
            SHIELD_REGAINED = BossMessage.simple("phase_shield_regained");

    private int tick;

    @Override
    public void onStart(EvokerBoss boss, BossContext context) {
        tick = 0;
        BossBar bar = boss.getPhaseBar();
        bar.setTitle("Shield down, attack!");
        bar.setColor(BarColor.YELLOW);
        bar.setVisible(true);

        EvokerEffects.shieldLoseEffect(
                boss.getWorld(),
                Vectors.doubleFrom(boss.getBossEntity().getLocation()),
                boss.getRoom()
        );

        boss.getRoom().getPlayers().forEach(bar::addPlayer);
        context.players().forEach(bar::addPlayer);

        boss.setInvulnerable(false);
        boss.getBossEntity().setGlowing(false);

        boss.broadcast(false, SHIELD_LOSE);
    }

    @Override
    public void onEnd(EvokerBoss boss, BossContext context) {
        boss.setInvulnerable(true);
        boss.getPhaseBar().setColor(BarColor.BLUE);

        if (boss.getState() != EvokerState.DYING) {
            boss.broadcast(false, SHIELD_REGAINED);

            Evoker entity = boss.getBossEntity();
            entity.setGlowing(true);
            EvokerEffects.shieldGainEffect(boss.getWorld(), Vectors.doubleFrom(entity.getLocation()), boss.getRoom());
        }

        tick = 0;
    }

    @Override
    public void onTick(EvokerBoss boss, BossContext context) {
        int maxTicks = EvokerConfig.vulnerable_length;
        tick++;

        if (tick > maxTicks) {
            boss.nextPhase(true);
            return;
        }

        double progress = (double) tick / (double) maxTicks;
        boss.getPhaseBar().setProgress(Math.min(progress, 1.0D));
    }
}