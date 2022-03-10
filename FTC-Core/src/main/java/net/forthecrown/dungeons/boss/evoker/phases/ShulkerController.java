package net.forthecrown.dungeons.boss.evoker.phases;

import com.destroystokyo.paper.ParticleBuilder;
import com.sk89q.worldedit.math.Vector3;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerEffects;
import net.forthecrown.dungeons.boss.evoker.EvokerVars;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;

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

            boolean shouldDraw = data.drawTick >= EvokerVars.SHULKER_DRAW_INTERVAL.get();
            if(shouldDraw) data.drawTick = 0;

            // Tick logic
            switch (data.phase) {
                case AIMING -> {
                    // Draw aiming beam
                    drawBeam(data);

                    // If we should start firing the projectile
                    if (data.tick >= EvokerVars.SHULKER_AIMING_TIME.get()) {
                        data.tick = 0;
                        data.phase = Phase.FIRING;
                        data.drawTick = EvokerVars.SHULKER_DRAW_INTERVAL.get();
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
                        Vector3 pos = data.target;
                        world.createExplosion(
                                pos.getX(), pos.getY(), pos.getZ(),
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
                    final int interval = EvokerVars.SHULKER_AIM_INTERVAL.get();
                    final double random = FtcUtils.RANDOM.nextDouble(interval);
                    final int nextAimTick = (int) (interval + random);

                    if (data.tick >= nextAimTick) {
                        data.tick = 0;
                        data.phase = Phase.AIMING;
                        data.drawTick = EvokerVars.SHULKER_DRAW_INTERVAL.get();
                        data.target = findTarget();

                        if (data.target == null) {
                            data.phase = Phase.NONE;
                        } else {
                            Vector3 dist = data.target.subtract(data.beamOrigin);
                            data.firingTicks = dist.length() / EvokerVars.FIRING_SPEED.get();

                            // beamStep is the large step the beam will take every
                            // tick towards the player
                            data.step =  dist.divide(data.firingTicks);
                            data.currentStart = data.beamOrigin;
                            data.particleDist = data.step.divide(PARTICLES_PER_STEP);
                        }
                    }
                }
            }
        }
    }

    public static final int PARTICLES_PER_STEP = 5;

    void drawFiringBeam(ShulkerData data) {
        Vector3 start = data.currentStart;
        Vector3 particleDist = data.particleDist;

        for (int i = 0; i < PARTICLES_PER_STEP; i++) {
            Vector3 pos = start.add(particleDist.multiply(i));

            new ParticleBuilder(Particle.REDSTONE)
                    .count(3)
                    .location(world, pos.getX(), pos.getY(), pos.getZ())
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
        Vector3 start = data.beamOrigin;
        Vector3 end = data.target;
        ParticleBuilder spawn = new ParticleBuilder(Particle.REDSTONE)
                .count(1)
                .color(data.phase.color, data.phase.size);

        Vector3 dif = end.subtract(start);
        double length = dif.length();
        dif = dif.normalize();

        for (double i = 0; i < length; i += EvokerVars.SHULKER_PARTICLE_DISTANCE.get()) {
            Vector3 vec = start.add(dif.multiply(i));

            spawn.location(world, vec.getX(), vec.getY(), vec.getZ())
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

    void setTraitor(ShulkerData data) {
        Shulker shulker = data.shulker;

        shulker.setColor(DyeColor.PURPLE);
        shulker.setInvulnerable(true);
        shulker.setAware(false);
        shulker.setPeek(1.0f);
        shulker.customName(Component.text("Shulker traitor"));

        EvokerEffects.shockwave(world, MathUtil.toWorldEdit(shulker.getLocation()), 1, false);

        data.phase = Phase.TRAITOR;
        data.target = MathUtil.toWorldEdit(boss.getBossEntity().getLocation())
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
            Vector3 pos = MathUtil.toWorldEdit(s.getLocation());

            new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                    .location(world, pos.getX(), pos.getY() + (s.getHeight() / 2) + 0.25, pos.getZ())
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

    Vector3 findTarget() {
        Player target = PotionPhase.findTarget(boss);
        if(target == null) return null;

        Vector3 pos = MathUtil.toWorldEdit(target.getLocation());
        Vector3 vel = MathUtil.toWorldEdit(target.getVelocity());

        return pos.add(vel)
                .add(0, target.getHeight() / 2, 0);
    }

    // Small data container for shulker stuff
    static class ShulkerData {
        final Shulker shulker;
        final Vector3 beamOrigin;

        Vector3 target, step, particleDist, currentStart;
        // Tick is used for general logic, drawTick for when the shulker's beam should be drawn
        int tick, drawTick;
        double firingTicks;
        Phase phase;

        public ShulkerData(Shulker shulker) {
            this.shulker = shulker;
            beamOrigin = MathUtil.toWorldEdit(shulker.getLocation()).add(0, shulker.getHeight() / 2, 0);
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
