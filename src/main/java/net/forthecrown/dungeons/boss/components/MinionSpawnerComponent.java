package net.forthecrown.dungeons.boss.components;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.utils.Util;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * A boss component that spawns minions
 */
public class MinionSpawnerComponent implements BossComponent<DungeonBoss> {
    private final MinionSpawner spawner;
    private final int spawnTickInterval, maxMinions;
    private final Vec3[] spawns;

    private final List<Entity> spawnedMinions = new ObjectArrayList<>();
    private int tick;

    public MinionSpawnerComponent(MinionSpawner spawner, int spawnTickInterval, int maxMinions, Vec3... spawns) {
        this.spawns = spawns;
        this.spawner = spawner;
        this.spawnTickInterval = spawnTickInterval;
        this.maxMinions = maxMinions;
    }

    // It's easier to type new 'double[][] { asdjkhaldsaa }' than to type 'new Vec3(asd, asd, asd)' a million times
    public static MinionSpawnerComponent create(MinionSpawner spawner, int tickInterval, int maxMinions, double[][] spawns) {
        Vec3[] vecSpawns = new Vec3[spawns.length];

        for (int i = 0; i < spawns.length; i++) {
            double[] spawn = spawns[i];

            // Ensure it has correct length
            Validate.isTrue(spawn.length == 3, "Size of vector %s at index %s is not 3", Arrays.toString(spawn), i);

            Vec3 vec = new Vec3(spawn[0], spawn[1], spawn[2]);
            vecSpawns[i] = vec;
        }

        return new MinionSpawnerComponent(spawner, tickInterval, maxMinions, vecSpawns);
    }

    public static MinionSpawnerComponent create(MinionSpawner spawner, int tickInterval, int maxMinions, Vec3... spawns) {
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
            Vec3 random = spawns[Util.RANDOM.nextInt(spawns.length)];

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
        @NotNull Entity create(Vec3 pos, World world, BossContext context);
    }
}