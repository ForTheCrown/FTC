package net.forthecrown.dungeons.boss.evoker;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.cosmetics.travel.TravelUtil;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

public final class EvokerEffects {
    private EvokerEffects() {}

    public static final ParticleBuilder IMPACT_PARTICLE = new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
            .count(5);

    public static void impactEffect(World world, Vector3d pos) {
        for (int i = 0; i < EvokerConfig.impact_stepCount; i++) {
            int finalI = i;

            Tasks.runLater(() -> drawImpact(world, pos, finalI), i * 10L);
        }

        shockwave(world, pos);
    }

    public static void drawImpact(World world, Vector3d pos, int step) {
        drawImpact(
                world, pos,
                0.2D,
                EvokerConfig.impact_radiusStart + (step * EvokerConfig.impact_radiusStep),
                (short) (step * EvokerConfig.impact_stepParticles)
        );
    }

    public static void drawImpact(World world, Vector3d pos, double yOffset, double radius, short points) {
        List<Vector3d> cirle = TravelUtil.getCirclePoints(yOffset, radius, points);

        for (Vector3d v: cirle) {
            Vector3d p = pos.add(v);

            IMPACT_PARTICLE.location(world, p.x(), p.y(), p.z())
                    .extra(0.0D)
                    .offset(0.25, 0.05, 0.25)
                    .allPlayers()
                    .spawn();
        }
    }

    public static void summoningEffect(World w, Vector3d pos, double height, double width) {
        flameEffect(w, pos, height);
        drawImpact(w, pos, 0.2D, (width / 2) + 0.5, EvokerConfig.impact_stepParticles);
        summoningSound(w, pos);
    }

    public static void summoningSound(World w, Vector3d pos) {
        w.playSound(
                Sound.sound(org.bukkit.Sound.BLOCK_CONDUIT_ACTIVATE, Sound.Source.MASTER, 1, 2),
                pos.x(), pos.y(), pos.z()
        );
    }

    public static void flameEffect(World w, Vector3d pos, double height) {
        double offsetHeight = (height + 0.25D) * 0.375;

        new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                .offset(0.25, offsetHeight, 0.25)
                .extra(0.0D)
                .count(25)
                .location(w, pos.x(), pos.y() + offsetHeight * 2 - 0.2, pos.z())
                .allPlayers()
                .spawn();
    }

    public static void shockwave(World world, Vector3d pos) {
        shockwave(world, pos, EvokerConfig.pushAway_radius, true);
    }

    public static void shockwave(World world, Vector3d pos, int radius, boolean bigExplosion) {
        WorldBounds3i pushArea = WorldBounds3i.of(
                new Location(world, pos.x(), pos.y(), pos.z()),
                radius
        );

        Vector bukkitPos = new Vector(pos.x(), pos.y(), pos.z());

        world.playSound(
                Sound.sound(org.bukkit.Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, Sound.Source.MASTER, 1f, 1f),
                pos.x(), pos.y(), pos.z()
        );

        new ParticleBuilder(bigExplosion ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE)
                .offset(1, 1, 1)
                .location(world, pos.x(), pos.y() + 1, pos.z())
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

    public static void shieldLoseEffect(World world, Vector3d vec, WorldBounds3i room) {
        playSoundInRoom(org.bukkit.Sound.BLOCK_CONDUIT_DEACTIVATE, room);
        world.spawnParticle(Particle.EXPLOSION_LARGE, vec.x(), vec.y(), vec.z(), 2);
    }

    public static void shieldGainEffect(World world, Vector3d vec, WorldBounds3i room) {
        impactEffect(world, vec);
        playSoundInRoom(org.bukkit.Sound.BLOCK_CONDUIT_ACTIVATE, room);
    }

    static void playSoundInRoom(org.bukkit.Sound s, WorldBounds3i room) {
        Sound sound = Sound.sound(s, Sound.Source.MASTER, 1, 1);

        for (Player p: room.getPlayers()) {
            p.playSound(sound);
        }
    }
}