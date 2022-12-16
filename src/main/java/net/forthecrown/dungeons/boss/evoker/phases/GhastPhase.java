package net.forthecrown.dungeons.boss.evoker.phases;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ghast;

public class GhastPhase implements AttackPhase {
    public static final double[][] SPAWNS = {
            { -277.5, 37, 38.5 },
            { -277.5, 37, 50.5 }
    };

    static final BossMessage START = BossMessage.simple("phase_ghast_start");

    private int tick;

    @Override
    public void onStart(EvokerBoss boss, BossContext context) {
        tick = 0;
        boss.getPhaseBar().setVisible(true);
        boss.getPhaseBar().setTitle("Ghasts, deflect their attacks!");

        boss.broadcast(false, START);

        for (double[] pos: SPAWNS) {
            Location l = new Location(boss.getWorld(), pos[0], pos[1], pos[2]);

            boss.getWorld().spawn(l, Ghast.class, ghast -> {
                Entity nms = VanillaAccess.getEntity(ghast);

                // explosionPower cannot be changed by setters, Bukkit doesn't
                // change this, so instead of using reflection, I just save
                // the ghast into NBT, modify the NBT, and then load the ghast
                // from that same NBT
                CompoundTag saved = new CompoundTag();
                nms.save(saved);

                saved.putByte("ExplosionPower", (byte) 3);
                nms.load(saved);

                AttackPhases.clearAllDrops(ghast);

                double health = EvokerConfig.ghast_health;
                AttributeInstance maxHealth = ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                Util.clearModifiers(maxHealth);

                maxHealth.setBaseValue(health);
                ghast.setHealth(health);
            });
        }
    }

    @Override
    public void onEnd(EvokerBoss boss, BossContext context) {

    }

    @Override
    public void onTick(EvokerBoss boss, BossContext context) {
        tick++;

        if(tick >= EvokerConfig.ghast_length) {
            boss.nextPhase(false);
        } else {
            double progress = (double) tick / (double) EvokerConfig.ghast_length;
            boss.getPhaseBar().setProgress(progress);
        }
    }
}