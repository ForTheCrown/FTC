package net.forthecrown.dungeons.boss.evoker;

import com.destroystokyo.paper.ParticleBuilder;
import com.sk89q.worldedit.math.Vector3;
import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.travel.TravelUtil;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public final class EvokerEffects {
    private EvokerEffects() {}

    public static final ParticleBuilder IMPACT_PARTICLE = new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
            .count(5);

    public static void impactEffect(World world, Vector3 pos) {
        for (int i = 0; i < EvokerVars.IMPACT_STEP_COUNT.get(); i++) {
            int finalI = i;

            Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
                drawImpact(world, pos, finalI);
            }, i * 10);
        }

        shockwave(world, pos);
    }

    public static void drawImpact(World world, Vector3 pos, int step) {
        drawImpact(
                world, pos,
                0.2D,
                EvokerVars.IMPACT_RADIUS_START.get() + (step * EvokerVars.IMPACT_RADIUS_STEP.get()),
                (short) (step * EvokerVars.IMPACT_STEP_PARICLES.get())
        );
    }

    public static void drawImpact(World world, Vector3 pos, double yOffset, double radius, short points) {
        List<Vector> cirle = TravelUtil.getOnCircle(yOffset, radius, points);

        for (Vector v: cirle) {
            Vector3 p = pos.add(v.getX(), v.getY(), v.getZ());

            IMPACT_PARTICLE.location(world, p.getX(), p.getY(), p.getZ())
                    .extra(0.0D)
                    .offset(0.25, 0.05, 0.25)
                    .allPlayers()
                    .spawn();
        }
    }

    public static void summoningEffect(World w, Vector3 pos, double height, double width) {
        flameEffect(w, pos, height);
        drawImpact(w, pos, 0.2D, (width / 2) + 0.5, EvokerVars.IMPACT_STEP_PARICLES.get());
        summoningSound(w, pos);
    }

    public static void summoningSound(World w, Vector3 pos) {
        w.playSound(
                Sound.sound(org.bukkit.Sound.BLOCK_CONDUIT_ACTIVATE, Sound.Source.MASTER, 1, 2),
                pos.getX(), pos.getY(), pos.getZ()
        );
    }

    public static void flameEffect(World w, Vector3 pos, double height) {
        double offsetHeight = (height + 0.25D) * 0.375;

        new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                .offset(0.25, offsetHeight, 0.25)
                .extra(0.0D)
                .count(25)
                .location(w, pos.getX(), pos.getY() + offsetHeight * 2 - 0.2, pos.getZ())
                .allPlayers()
                .spawn();
    }

    public static void shockwave(World world, Vector3 pos) {
        shockwave(world, pos, EvokerVars.PUSH_AWAY_RADIUS.get(), true);
    }

    public static void shockwave(World world, Vector3 pos, int radius, boolean bigExplosion) {
        FtcBoundingBox pushArea = FtcBoundingBox.of(world, Vector3i.of(pos.getX(), pos.getY(), pos.getZ()), radius);
        Vector bukkitPos = new Vector(pos.getX(), pos.getY(), pos.getZ());

        world.playSound(
                Sound.sound(org.bukkit.Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, Sound.Source.MASTER, 1f, 1f),
                pos.getX(), pos.getY(), pos.getZ()
        );

        new ParticleBuilder(bigExplosion ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE)
                .offset(1, 1, 1)
                .location(world, pos.getX(), pos.getY() + 1, pos.getZ())
                .allPlayers()
                .count(5)
                .spawn();

        for (Player p: pushArea.getPlayers()) {
            Vector velocity = bukkitPos.clone()
                    .subtract(p.getLocation().toVector())
                    .multiply(-0.5F)
                    .normalize();

            p.setVelocity(p.getVelocity().add(velocity));
        }
    }

    public static void shieldLoseEffect(World world, Vector3 vec, FtcBoundingBox room) {
        playSoundInRoom(org.bukkit.Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1f, room);
        world.spawnParticle(Particle.EXPLOSION_LARGE, vec.getX(), vec.getY(), vec.getZ(), 2);
    }

    public static void shieldGainEffect(World world, Vector3 vec, FtcBoundingBox room) {
        impactEffect(world, vec);
        playSoundInRoom(org.bukkit.Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1f, room);
    }

    static void playSoundInRoom(org.bukkit.Sound s, float vol, float pitch, FtcBoundingBox room) {
        Sound sound = Sound.sound(s, Sound.Source.MASTER, vol, pitch);

        for (Player p: room.getPlayers()) {
            p.playSound(sound);
        }
    }
}