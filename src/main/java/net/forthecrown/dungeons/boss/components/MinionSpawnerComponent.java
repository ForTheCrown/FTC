package net.forthecrown.dungeons.boss.components;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.utils.Util;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;

import java.util.Arrays;
import java.util.List;

/**
 * A boss component that spawns minions
 */
public class MinionSpawnerComponent implements BossComponent<DungeonBoss> {
    private final MinionSpawner spawner;
    private final int spawnTickInterval, maxMinions;
    private final Vector3d[] spawns;

    private final List<Entity> spawnedMinions = new ObjectArrayList<>();
    private int tick;

    public MinionSpawnerComponent(MinionSpawner spawner, int spawnTickInterval, int maxMinions, Vector3d... spawns) {
        this.spawns = spawns;
        this.spawner = spawner;
        this.spawnTickInterval = spawnTickInterval;
        this.maxMinions = maxMinions;
    }

    // It's easier to type new 'double[][] { asdjkhaldsaa }' than to type 'new Vec3(asd, asd, asd)' a million times
    public static MinionSpawnerComponent create(MinionSpawner spawner, int tickInterval, int maxMinions, double[][] spawns) {
        Vector3d[] vecSpawns = new Vector3d[spawns.length];

        for (int i = 0; i < spawns.length; i++) {
            double[] spawn = spawns[i];

            // Ensure it has correct length
            Validate.isTrue(spawn.length == 3, "Size of vector %s at index %s is not 3", Arrays.toString(spawn), i);

            Vector3d vec = new Vector3d(spawn[0], spawn[1], spawn[2]);
            vecSpawns[i] = vec;
        }

        return new MinionSpawnerComponent(spawner, tickInterval, maxMinions, vecSpawns);
    }

    public static MinionSpawnerComponent create(MinionSpawner spawner, int tickInterval, int maxMinions, Vector3d... spawns) {
        return new MinionSpawnerComponent(spawner, tickInterval, maxMinions, spawns);
    }

    @Override
    public void onTick(DungeonBoss boss, BossContext context) {
        tick++;

        // If we've reached a spawning tick
        if (tick > spawnTickInterval) {
            tick = 0; // reset tick count

            // If we're allowed to spawn a minion
            if(spawnedMinions.size() >= maxMinions) return;

            // Pick random spawn and spawn there with the
            // given spawner
            Vector3d random = spawns[Util.RANDOM.nextInt(spawns.length)];

            Entity e = spawner.create(random, boss.getWorld(), context);
            spawnedMinions.add(e);
        }
    }

    @Override
    public void onDeath(DungeonBoss boss, BossContext context, boolean forced) {
        //Apply withering effect to all minions
        for (Entity e: spawnedMinions) {
            kill(e);
        }

        // Reset tick and minion list
        spawnedMinions.clear();
        tick = 0;
    }

    public static void kill(Entity e) {
        if (!(e instanceof LivingEntity living)) return;

        living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 99999, 254, false, false));
    }

    /**
     * A small interface to allow for minion
     * spawning
     */
    @FunctionalInterface
    public interface MinionSpawner {
        /**
         * Spawns a minion
         * @param pos The position to spawn the minion at
         * @param world The world to spawn them in
         * @param context The boss fight's context
         *
         * @return The spawned minion
         */
        @NotNull Entity create(Vector3d pos, World world, BossContext context);
    }
}