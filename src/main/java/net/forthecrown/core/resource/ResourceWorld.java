package net.forthecrown.core.resource;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import net.forthecrown.core.*;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.CmdUsables;
import net.forthecrown.useables.command.Warp;
import net.forthecrown.useables.test.TestCooldown;
import net.forthecrown.useables.test.TestWorld;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.craftbukkit.v1_19_R1.CraftHeightMap;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Gates;
import org.spongepowered.math.vector.Vector3i;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.forthecrown.core.FtcDiscord.C_RW;
import static net.forthecrown.core.config.ResourceWorldConfig.*;

@Getter
public class ResourceWorld implements DayChangeListener {
    private static final Logger LOGGER = Crown.logger();

    private static final ResourceWorld INSTANCE = new ResourceWorld();

    /**
     * The distance a biome and flatness check should go,
     * in QuartPos distance aka bit shifted twice to the right
     */
    public static final int SPAWN_CHECK_QUART   = 5;

    /**
     * The maximum Y a spawn can generate at, if we're above it,
     * then we're most likely in a hilly area that is unfit to
     * for a spawn
     */
    public static final int MAX_Y               = 75;

    /** The amount of WG region is bigger than the spawn */
    public static final int WG_OVERREACH        = 5;

    /**
     * The amount the WG region is bigger than the spawn,
     * on the Y axis, goes from MAX_Y to spawn_y_pos - WG_SIZE_Y
     */
    public static final int WG_SIZE_Y           = 20;

    /** The max Y difference a potential spawn position can have */
    public static final int MAX_Y_DIF           = 2;

    /** The maximum amount of attempts that can be made to find a seed */
    public static final int MAX_SEED_ATTEMPTS   = 1024;

    /**
     * All legal biomes that spawn can be in
     */
    public static final Set<ResourceKey<Biome>> SPAWN_BIOMES = ObjectSet.of(
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.SNOWY_PLAINS,
            Biomes.DESERT
    );

    /** Required biome tags any potential seed must have within its world borders */
    public static final Set<TagKey<Biome>> REQUIRED_TAGS = ObjectSet.of(
            BiomeTags.IS_FOREST,
            BiomeTags.IS_MOUNTAIN,
            BiomeTags.IS_TAIGA,
            BiomeTags.IS_SAVANNA,
            BiomeTags.IS_JUNGLE,
            BiomeTags.IS_BADLANDS,
            BiomeTags.IS_DEEP_OCEAN,
            BiomeTags.IS_OCEAN,
            BiomeTags.IS_RIVER,
            BiomeTags.IS_BEACH,
            BiomeTags.HAS_IGLOO,
            BiomeTags.HAS_VILLAGE_SNOWY
    );

    // The height maps for NMS and Bukkit that are used for height calculation... shocking ik
    public static final Heightmap.Types HEIGHT_MAP_TYPE = Heightmap.Types.OCEAN_FLOOR_WG;
    public static final HeightMap BUKKIT_HEIGHT_MAP = CraftHeightMap.fromNMS(HEIGHT_MAP_TYPE);

    public static final String PORTAL_WARP = "portal";

    /**
     * An accessor that ChunkGenerator needs for a height check call
     */
    public static final LevelHeightAccessor HEIGHT_ACCESSOR = LevelHeightAccessor.create(Util.MIN_Y, Util.Y_SIZE);

    /* ----------------------------- INSTANCING ------------------------------ */

    private ResourceWorld() {
        DayChange.get().addListener(this);
    }

    public static ResourceWorld get() {
        return INSTANCE;
    }

    /* ----------------------------- METHODS ------------------------------ */

    public void resetAndLoad() {
        if (!Structures.get().getRegistry().contains(spawnStructure)) {
            LOGGER.error("Cannot start RW reset, no spawn structure with key '{}' found", spawnStructure);
            return;
        }

        LOGGER.info("Starting RW reset");

        World original = Worlds.resource();

        // Kick players out and close gates
        WorldReCreator.kickPlayers(original);
        setGatesOpen(false);

        // Attempt to announce closing
        if (resetStart == null) {
            LOGGER.warn("resetStart message is null, cannot announce");
        } else {
            Crown.getAnnouncer().announce(resetStart);
        }

        findSeed().whenComplete((seed, throwable) -> {
            if (throwable != null) {
                StaffChat.send(Component.text("Error while attempting to find RW seed"), false);
                FtcDiscord.staffLog(C_RW, "Error while attempting to find RW seed");
                LOGGER.error("Error while attempting to find seed, cannot open RW", throwable);

                return;
            }

            Bukkit.getScheduler().runTask(Crown.plugin(), () -> {
                // Re-create world
                WorldReCreator creator = WorldReCreator.of(original)
                        .seed(seed)
                        .preserveGameRules(true)
                        .preserveWorldBorder(true);

                World newWorld = creator.run();
                WorldBorder border = VanillaAccess.getLevel(newWorld).getWorldBorder();
                border.setSize(nextSize);

                lastSeed = newWorld.getSeed();

                WorldLoader.loadAsync(newWorld)
                        .whenComplete(this::onWorldLoadedAsync);
            });

            // Purge RW sections
            ResourceWorldTracker.get().reset();
        });
    }

    private void onWorldLoadedAsync(World world, Throwable throwable) {
        if (throwable != null) {
            StaffChat.send(Component.text("Error while resetting RW, cannot finish"), false);
            LOGGER.error("Could not regen Resource World", throwable);
            FtcDiscord.staffLog(C_RW, "Error while resetting Resource World!");
            return;
        }

        var spawnOptional = Structures.get()
                .getRegistry()
                .get(spawnStructure);

        if (spawnOptional.isEmpty()) {
            LOGGER.error("Cannot find RW spawn, no structure with key {} found", spawnStructure);
            return;
        }

        BlockStructure spawn = spawnOptional.get();

        // Run rest of the code in sync with the server
        Tasks.runSync(() -> onWorldLoadedSync(world, spawn));
    }

    private void onWorldLoadedSync(World world, BlockStructure spawn) {
        int y = world.getHighestBlockYAt(0, 0, BUKKIT_HEIGHT_MAP) + 1;

        // Take the size of the spawn structure, divide in half
        // and make the vector negative, set the y to first empty
        // Y block, we've now got a place position for the spawn
        Vector3i placePos = spawn.getPalette(null).getSize()
                .div(-2)
                .withY(y);

        StructurePlaceConfig config = StructurePlaceConfig.builder()
                .placeEntities(true)
                .addNonNullProcessor()
                .addRotationProcessor()
                .world(world)
                .pos(placePos)
                .build();

        // Place spawn structure
        spawn.place(config);
        LOGGER.info("Placed rw spawn at {}", config.getDestination());

        Vector3i minUnder = placePos.withY(y - 1);
        Vector3i maxUnder = placePos.mul(-1)
                .withY(y - (WG_SIZE_Y >> 1))
                .sub(1, 0, 1);

        // Make sure there are no air blocks under
        // the spawn
        WorldBounds3i underArea = WorldBounds3i.of(world, minUnder, maxUnder);
        Orientable data = (Orientable) Material.STRIPPED_DARK_OAK_WOOD.createBlockData();
        data.setAxis(Axis.X);

        for (Block b : underArea) {
            // Don't replace solid blocks
            if (b.isSolid()) {
                continue;
            }

            b.setBlockData(data, false);
        }

        // Move gates' locations
        Location gateLocation = new Location(world, -6, y + 1, 0, -90, 0);
        Location destination = new Location(world, 0, y + 2, 0);

        world.setSpawnLocation(destination);

        Gate toHaz = Gate.get(toHazGate);
        Gate toRes = Gate.get(toResGate);

        // Move Haz -> RW gate from position
        if (toHaz == null) {
            LOGGER.warn("Could not re-position rw -> haz gate, could not find gate with id {}", toHazGate);
        } else {
            // First null forces the 'from' list to be
            // emptied, second method call actually sets
            // the 'from' location. Don't ask me why it's
            // like this, this was in /setfrom's code
            toHaz.addFrom(null);
            toHaz.addFrom(gateLocation);

            LOGGER.info("Moved rw -> haz gate to {}", Vectors.fromI(gateLocation));
        }

        // Move RW -> Haz gate destination
        if (toRes == null) {
            LOGGER.warn("Could not re-position haz -> rw gate, could not find gate with id {}", toResGate);
        } else {
            // Same deal with the 2 method calls as above
            toRes.addTo(null);
            toRes.addTo(destination);

            LOGGER.info("Moved haz -> rw destination to {}", Vectors.fromI(destination));
        }

        // Move portal warp or create it
        CmdUsables<Warp> warps = Usables.get().getWarps();
        Warp portalWarp = warps.get(PORTAL_WARP);

        if (portalWarp == null) {
            // Check doesn't exist, create it
            portalWarp = new Warp(PORTAL_WARP, destination);

            // Make sure it has the in_world check and cooldown
            var checks = portalWarp.getChecks();
            checks.add(new TestCooldown(TimeUnit.MINUTES.toMillis(5)));
            checks.add(new TestWorld(world));

            warps.add(portalWarp);
        } else {
            portalWarp.setDestination(destination);
        }

        // Redefine WorldGuard region to match spawn bounds
        if (!Util.isNullOrBlank(worldGuardSpawn)) {
            // Figure out bounds
            BlockVector3 min = BlockVector3.at(
                    placePos.x() - WG_OVERREACH,
                    y - WG_SIZE_Y,
                    placePos.z() - WG_OVERREACH
            );

            BlockVector3 max = min.abs().withY(Util.MAX_Y);

            // Get manager and region
            RegionManager manager = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .get(BukkitAdapter.adapt(world));

            ProtectedRegion region = manager.getRegion(worldGuardSpawn);

            // Region might be null, idk how that might happen but still, just in case
            if (region == null) {
                LOGGER.info("Couldn't find world guard spawn region, creating...");

                region = new ProtectedCuboidRegion(worldGuardSpawn, min, max);
            } else {
                LOGGER.info("Moving world guard spawn region to match new RW spawn");

                // This is apparently how region redefining is done, you remove
                // the region, create a new one and copy everything from the last
                // one and put the new region back into the manager
                manager.removeRegion(region.getId(), RemovalStrategy.REMOVE_CHILDREN);
                ProtectedRegion old = region;

                region = new ProtectedCuboidRegion(worldGuardSpawn, min, max);
                region.copyFrom(old);
            }

            manager.addRegion(region);
        } else {
            LOGGER.warn("wgSpawnName in resourceWorld is null, cannot edit spawn region");
        }

        // Attempt to announce the end's reset being finished
        if (resetEnd == null) {
            LOGGER.warn("resetEnd message is null, cannot announce opening");
        } else {
            Crown.getAnnouncer().announce(resetEnd);
        }

        setGatesOpen(true);

        lastReset = System.currentTimeMillis();
        LOGGER.info("RW reset finished");

        FtcDiscord.staffLog(C_RW, "Resource World reset finished!");
    }

    private void setGatesOpen(boolean open) {
        setOpen(toResGate, open, "Cannot {} res -> haz gate, no gate with {} ID found");
        setOpen(toHazGate, open, "Cannot {} haz -> res gate, no gate with {} ID found");

        Gate.save();

        String status = open ? "open" : "close";
        LOGGER.info("setGatesOpen set, status: {}", status);
    }

    private void setOpen(String id, boolean open, String format) {
        Gate g = Gate.get(id);

        if (g == null) {
            LOGGER.warn(format, open ? "open" : "close", id);
            return;
        }

        if (open) {
            Gates.open(g);
        } else {
            Gates.close(g);
        }
    }

    @Override
    public void onDayChange(ZonedDateTime time) {
        if (!enabled) {
            return;
        }

        if (!Time.isPast(Vars.rwResetInterval + lastReset)) {
            return;
        }

        if (WorldLoader.isLoading(Worlds.end())) {
            LOGGER.warn("End is already regenerating, moving RW reset ahead by one day");
            return;
        }

        resetAndLoad();
    }

    // This method in its current state takes a fair amount of power
    // to complete, if we'd run it soley on the main thread we'd end
    // up crashing it, so we run it async to avoid holding up the main
    // thread
    private CompletableFuture<Long> findSeed() {
        return CompletableFuture.supplyAsync(() -> {
            // If we have any set seeds, aka 'legal seeds' then use those over
            // randomly generated seeds
            if (!legalSeeds.isEmpty()) {
                // If we only have 1 seed, use that one lol
                if (legalSeeds.size() == 1) {
                    return legalSeeds.getLong(0);
                }

                long result = legalSeeds.getLong(Util.RANDOM.nextInt(legalSeeds.size()));

                // Loop through set seeds until we find one that isn't
                // the last seed we used
                while (result == lastSeed) {
                    result = legalSeeds.getLong(Util.RANDOM.nextInt(legalSeeds.size()));
                }

                return result;
            }

            // Random seed finder
            long seed = Util.RANDOM.nextLong();
            short safeGuard = 0;

            while (!isAcceptableSeed(seed)) {
                seed = Util.RANDOM.nextLong();

                safeGuard++;

                if (safeGuard > MAX_SEED_ATTEMPTS) {
                    LOGGER.warn("Couldn't find good seed for ResourceWorld, returning unfit one");
                    break;
                }
            }

            LOGGER.info("Took {} loops to find seed", safeGuard);
            return seed;
        });
    }

    private boolean isAcceptableSeed(long seed) {
        if (seed == lastSeed) {
            return false;
        }

        // Create chunk generator for given seed
        WorldGenSettings settings = WorldPresets.createNormalWorldFromPreset(DedicatedServer.getServer().registryHolder, seed);
        NoiseBasedChunkGenerator gen = (NoiseBasedChunkGenerator) settings.overworld();
        RandomState randomState = RandomState.create(gen.settings.value(), gen.noises, seed);

        int baseY = gen.getBaseHeight(0, 0, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR, randomState);

        // Ensure the seed has all required biomes
        // As far as I can see, this is also the most intense part
        // of the seed testing in terms of power needed to run it
        if (!hasBiomes(gen, nextSize / 2, QuartPos.fromBlock(baseY), randomState)) {
            return false;
        }

        // In most cases, if we're above 75, we're in a hilly area
        // Hilly areas are a no no
        if (baseY > MAX_Y) {
            return false;
        }

        // a ~12 block radius in and around the spawn
        // to make sure it has fitting biomes and is
        // generally flat enough
        for (int x = 0; x < SPAWN_CHECK_QUART; x++) {
            for (int z = 0; z < SPAWN_CHECK_QUART; z++) {
                if (!isAreaGood(x, z, gen, baseY, randomState)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isAreaGood(int x, int z, NoiseBasedChunkGenerator gen, int baseY, RandomState randomState) {
        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);

        // Get the difference between this area's
        // Y level and the base Y
        int y = gen.getBaseHeight(blockX, blockZ, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR, randomState);
        int dif = baseY - y;

        // Biome's use their own positioning,
        // which is 1/4 the size of a chunk
        Holder<Biome> b = gen.getBiomeSource().getNoiseBiome(
                x, QuartPos.fromBlock(baseY), z,
                randomState.sampler()
        );

        // For this to return true, the biome must be from
        // an acceptable category as well as having a height
        // difference from the base less than MAX_Y_DIF
        return b.is(SPAWN_BIOMES::contains)
                && dif <= MAX_Y_DIF
                && dif >= -MAX_Y_DIF;
    }


    private boolean hasBiomes(NoiseBasedChunkGenerator gen, int halfSize, int y, RandomState randomState) {
        int max = QuartPos.fromBlock(halfSize);
        int min = QuartPos.fromBlock(-halfSize);

        Set<TagKey<Biome>> requiredTags = new ObjectOpenHashSet<>();
        requiredTags.addAll(REQUIRED_TAGS);

        // Go through the world area and find the biome at every
        // cord. For the sake of speed, it only gets every 8th biome.
        // Going through each biome might mean as many as 67,000
        // iterations which is not doable lol, this is more like 8,000
        // which is still a lot, but more manageable than not skipping
        // every 8 chunks
        for (int x = min; x < max; x += 8) {
            for (int z = min; z < max; z += 8) {
                // Remove all this biome's tags from the set of
                // tags that haven't been found yet
                var holder = gen.getBiomeSource().getNoiseBiome(x, y, z, randomState.sampler());
                holder.tags().forEach(requiredTags::remove);

                // set is empty, means we've found biomes with
                // all of the required tags
                if (requiredTags.isEmpty()) {
                    return true;
                }
            }
        }

        return requiredTags.isEmpty();
    }
}