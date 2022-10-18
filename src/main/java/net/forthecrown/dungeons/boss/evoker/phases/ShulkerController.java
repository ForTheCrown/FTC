package net.forthecrown.dungeons.boss.evoker.phases;

import com.destroystokyo.paper.ParticleBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerEffects;
import net.forthecrown.dungeons.boss.evoker.EvokerVars;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

// I'm something of a data-oriented programmer myself
public class ShulkerController {
    final World world;
    final EvokerBoss boss;
    final BossContext context;
    final ShulkerPhase phase;
    final List<ShulkerData> shulkers = new ObjectArrayList<>();

    public ShulkerController(ShulkerPhase phase, EvokerBoss boss, BossContext context) {
        this.phase = phase;
        this.boss = boss;
        this.context = context;
        this.world = boss.getWorld();
    }

    public void tick() {
        for (ShulkerData data : shulkers) {
            // Tick incrementing
            data.tick++;
            if (data.phase != Phase.NONE) {
                data.drawTick++;
            }

            boolean shouldDraw = data.drawTick >= EvokerVars.shulker_drawInterval;
            if(shouldDraw) data.drawTick = 0;

            // Tick logic
            switch (data.phase) {
                case AIMING -> {
                    // Draw aiming beam
                    drawBeam(data);

                    // If we should start firing the projectile
                    if (data.tick >= EvokerVars.shulker_aimingTime) {
                        data.tick = 0;
                        data.phase = Phase.FIRING;
                        data.drawTick = EvokerVars.shulker_drawInterval;
                    }
                }

                case FIRING -> {
                    data.currentStart = data.currentStart.add(data.step);

                    drawFiringBeam(data);

                    if (data.tick >= data.firingTicks) {
                        data.phase = Phase.NONE;
                        data.tick = 0;
                        data.drawTick = 0;

                        // Boom boom at target :D
                        Vector3d pos = data.target;
                        world.createExplosion(
                                pos.x(), pos.y(), pos.z(),
                                3.5f, false, false, data.shulker
                        );
                    }
                }

                case TRAITOR -> {
                    if (shouldDraw) {
                        drawBeam(data);
                    }
                }

                case NONE -> {
                    final int interval = EvokerVars.shulker_aimInterval;
                    final double random = Util.RANDOM.nextDouble(interval);
                    final int nextAimTick = (int) (interval + random);

                    if (data.tick >= nextAimTick) {
                        data.tick = 0;
                        data.phase = Phase.AIMING;
                        data.drawTick = EvokerVars.shulker_drawInterval;
                        data.target = findTarget();

                        if (data.target == null) {
                            data.phase = Phase.NONE;
                        } else {
                            Vector3d dist = data.target.sub(data.beamOrigin);
                            data.firingTicks = dist.length() / EvokerVars.shulker_firingSpeed;

                            // beamStep is the large step the beam will take every
                            // tick towards the player
                            data.step =  dist.div(data.firingTicks);
                            data.currentStart = data.beamOrigin;
                            data.particleDist = data.step.div(PARTICLES_PER_STEP);
                        }
                    }
                }
            }
        }
    }

    public static final int PARTICLES_PER_STEP = 5;

    void drawFiringBeam(ShulkerData data) {
        Vector3d start = data.currentStart;
        Vector3d particleDist = data.particleDist;

        for (int i = 0; i < PARTICLES_PER_STEP; i++) {
            Vector3d pos = start.add(particleDist.mul(i));

            new ParticleBuilder(Particle.REDSTONE)
                    .count(3)
                    .location(world, pos.x(), pos.y(), pos.z())
                    .allPlayers()
                    .data(new Particle.DustTransition(
                            data.phase.color,
                            Color.fromRGB(43, 30, 30),
                            data.phase.size
                    ))
                    .spawn();
        }
    }

    void drawBeam(ShulkerData data) {
        Vector3d start = data.beamOrigin;
        Vector3d end = data.target;
        ParticleBuilder spawn = new ParticleBuilder(Particle.REDSTONE)
                .count(1)
                .color(data.phase.color, data.phase.size);

        Vector3d dif = end.sub(start);
        double length = dif.length();
        dif = dif.normalize();

        for (double i = 0; i < length; i += EvokerVars.shulker_particleDistance) {
            Vector3d vec = start.add(dif.mul(i));

            spawn.location(world, vec.x(), vec.y(), vec.z())
                    .allPlayers()
                    .spawn();
        }
    }

    ShulkerData get(Shulker s) {
        for (ShulkerData d: shulkers) {
            if(d.shulker.equals(s)) return d;
        }

        return null;
    }

    void betray(ShulkerData data) {
        Shulker shulker = data.shulker;

        shulker.setColor(DyeColor.PURPLE);
        shulker.setInvulnerable(true);
        shulker.setAware(false);
        shulker.setPeek(1.0f);
        shulker.customName(Component.text("Shulker traitor"));

        EvokerEffects.shockwave(world, Vectors.fromD(shulker.getLocation()), 1, false);

        data.phase = Phase.TRAITOR;
        data.target = Vectors.fromD(boss.getBossEntity().getLocation())
                .add(0, boss.getBossEntity().getHeight() / 2, 0);

        // Check if all shulkers are traitors
        for (ShulkerData d: shulkers) {
            if(d.phase != Phase.TRAITOR) return;
        }

        phase.onCompleteBetrayal(boss);
    }

    void removeAll() {
        for (ShulkerData d: shulkers) {
            Shulker s = d.shulker;
            Vector3d pos = Vectors.fromD(s.getLocation());

            new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                    .location(world, pos.x(), pos.y() + (s.getHeight() / 2) + 0.25, pos.z())
                    .offset(s.getWidth() / 2 + 0.25, s.getHeight() / 2, s.getWidth() / 2 + 0.25)
                    .allPlayers()
                    .spawn();

            s.remove();
        }

        shulkers.clear();
    }

    double progress() {
        double max = 0;
        double progress = 0;

        for (ShulkerData s: shulkers) {
            progress += s.phase == Phase.TRAITOR ? 0 : s.shulker.getHealth();
            max += s.shulker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }

        return progress / max;
    }

    Vector3d findTarget() {
        Player target = PotionPhase.findTarget(boss);
        if(target == null) return null;

        Vector3d pos = Vectors.fromD(target.getLocation());
        Vector3d vel = Vectors.fromD(target.getVelocity());

        return pos.add(vel)
                .add(0, target.getHeight() / 2, 0);
    }

    // Small data container for shulker stuff
    static class ShulkerData {
        final Shulker shulker;
        final Vector3d beamOrigin;

        Vector3d target, step, particleDist, currentStart;
        // Tick is used for general logic, drawTick for when the shulker's beam should be drawn
        int tick, drawTick;
        double firingTicks;
        Phase phase;

        public ShulkerData(Shulker shulker) {
            this.shulker = shulker;
            beamOrigin = Vectors.fromD(shulker.getLocation()).add(0, shulker.getHeight() / 2, 0);
            phase = Phase.NONE;
        }
    }

    // The phase a shulker is at
    enum Phase {
        NONE (null, 0f),
        AIMING (Color.WHITE, 1f),
        FIRING (Color.RED, 3f),
        TRAITOR (Color.PURPLE, 4f);

        final Color color;
        final float size;

        Phase(Color color, float size) {
            this.color = color;
            this.size = size;
        }
    }
}