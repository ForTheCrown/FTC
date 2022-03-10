package net.forthecrown.dungeons.boss.evoker.phases;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerEffects;
import net.forthecrown.dungeons.boss.evoker.EvokerVars;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.utils.FtcUtils;
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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PotionPhase implements AttackPhase {
    private static final List<QueuedPotion> QUEUED_POTIONS = new ObjectArrayList<>();

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

        double progress = (double) tick / (double) EvokerVars.POT_PHASE_LENGTH.get();
        boss.getPhaseBar().setProgress(progress);

        if(tick >= EvokerVars.POT_PHASE_LENGTH.get()) {
            boss.nextPhase(true);
            return;
        }

        if(!QUEUED_POTIONS.isEmpty()) {
            Iterator<QueuedPotion> iterator = QUEUED_POTIONS.iterator();

            while (iterator.hasNext()) {
                QueuedPotion e = iterator.next();
                e.untilSpawn--;

                if(e.untilSpawn <= 0) {
                    Vector2 next = e.pos;
                    Location l = new Location(boss.getWorld(), next.getX(), EvokerVars.POT_SPAWN_Y.get(), next.getZ());
                    int duration = EvokerVars.POT_PHASE_LENGTH.get() - tick;

                    l.getWorld().spawn(l, ThrownPotion.class, potion -> {
                        ItemStack item = new ItemStackBuilder(Material.LINGERING_POTION, 1)
                                .addEffect(new PotionEffect(
                                        FtcUtils.RANDOM.nextBoolean() ? PotionEffectType.POISON : PotionEffectType.WITHER,
                                        duration, (context.modifier() >= (float) FtcVars.maxBossDifficulty.get() / 2) ? 2 : 1,
                                        false, true, true
                                ))
                                .build();

                        potion.setItem(item);
                    });

                    iterator.remove();
                }
            }
        }

        if(potionTick < EvokerVars.POT_THROW_INTERVAL.get()) return;
        potionTick = 0;

        Vector2 potionSpawn;

        if(FtcUtils.RANDOM.nextBoolean()) {
            Player p = findTarget(boss);
            if(p == null) {
                Crown.logger().warn("Found no targets for PotionPhase");
                return;
            }

            Location l = p.getLocation();
            potionSpawn = Vector2.at(l.getX(), l.getZ());
        } else {
            potionSpawn = Vector2.at(generateCord(), generateCord());
        }

        QUEUED_POTIONS.add(new QueuedPotion(potionSpawn));
        Vector3 pos = potionSpawn.toVector3(EvokerVars.POT_SPAWN_Y.get());

        EvokerEffects.summoningSound(boss.getWorld(), pos);
        EvokerEffects.drawImpact(boss.getWorld(), pos, 1);
    }

    private static double generateCord() {
        double result = FtcUtils.RANDOM.nextDouble(
                EvokerVars.POT_DIST_MIN.get(),
                EvokerVars.POT_DIST_MAX.get() + 1
        );

        return FtcUtils.RANDOM.nextBoolean() ? result : -result;
    }

    static Player findTarget(EvokerBoss boss) {
        return FtcUtils.RANDOM.pickRandomEntry(
                boss.getRoom().getPlayers()
                        .stream()
                        .filter(player ->
                                (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
                                && !player.isDead()
                        )
                        .collect(Collectors.toList())
        );
    }

    private static class QueuedPotion {
        private final Vector2 pos;
        private int untilSpawn = EvokerVars.POT_SPAWN_DELAY.get();

        public QueuedPotion(Vector2 pos) {
            this.pos = pos;
        }
    }
}
