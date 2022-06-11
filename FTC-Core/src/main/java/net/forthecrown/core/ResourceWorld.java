package net.forthecrown.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.structure.BlockPlacer;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.EntityPlacer;
import net.forthecrown.structure.StructurePlaceContext;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.craftbukkit.v1_18_R2.CraftHeightMap;
import org.bukkit.entity.Player;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Gates;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

import static net.forthecrown.core.FtcDiscord.C_RW;

public class ResourceWorld extends FtcConfig.ConfigSection implements DayChangeListener {
    private static final Logger LOGGER = Crown.logger();

    /**
     * The distance a biome and flatness check should go,
     * in QuartPos distance aka bit shifted twice to the right
     */
    public static final int
            SPAWN_CHECK_QUART   = 5,

            /**
             * The maximum Y a spawn can generate at, if we're above it,
             * then we're most likely in a hilly area that is unfit to
             * for a spawn
             */
            MAX_Y               = 75,

            /**
             * The amount of WG region is bigger than the spawn
             */
            WG_OVERREACH        = 5,

            /**
             * The amount the WG region is bigger than the spawn,
             * on the Y axis, goes from MAX_Y to spawn_y_pos - WG_SIZE_Y
             */
            WG_SIZE_Y           = 20,

            /**
             * The max Y difference a potential spawn position can have
             */
            MAX_Y_DIF           = 2;

    /**
     * All legal biome categories that spawn can be in
     */
    public static final EnumSet<Biome.BiomeCategory> LEGAL_CATEGORIES = EnumSet.of(
            Biome.BiomeCategory.PLAINS,
            Biome.BiomeCategory.DESERT
    );

    /**
     * Required biome types any potential seed must have within its world borders
     */
    public static final EnumSet<Biome.BiomeCategory> REQUIRED_CATEGORIES = EnumSet.of(
            Biome.BiomeCategory.DESERT,
            Biome.BiomeCategory.FOREST,
            Biome.BiomeCategory.MESA,
            Biome.BiomeCategory.TAIGA,
            Biome.BiomeCategory.JUNGLE,
            Biome.BiomeCategory.SAVANNA,
            Biome.BiomeCategory.MOUNTAIN
    );

    // The height maps for NMS and Bukkit that are used for height calculation... shocking ik
    public static final Heightmap.Types HEIGHT_MAP_TYPE = Heightmap.Types.OCEAN_FLOOR_WG;
    public static final HeightMap BUKKIT_HEIGHT_MAP = CraftHeightMap.fromNMS(HEIGHT_MAP_TYPE);

    /**
     * An accessor that ChunkGenerator needs for a height check call
     */
    public static final LevelHeightAccessor HEIGHT_ACCESSOR = LevelHeightAccessor.create(FtcUtils.MIN_Y, FtcUtils.Y_SIZE);

    @Getter
    private final LongList legalSeeds = new LongArrayList();

    @Getter @Setter
    private String toHazGate, toResGate, worldGuardSpawn;
    @Getter @Setter
    private Component resetStart, resetEnd;
    @Getter @Setter
    private long lastReset, lastSeed;
    @Getter
    private int size;
    @Getter @Setter
    private boolean autoResetEnabled;
    @Getter @Setter private Key spawnStructure;

    ResourceWorld() {
        super("resource_world");
    }

    public void resetAndLoad() {
        if (!Registries.STRUCTURES.contains(spawnStructure)) {
            LOGGER.error("Cannot start RW reset, no spawn structure with id '{}' found", spawnStructure);
            return;
        }

        LOGGER.info("Starting RW reset");

        World original = Worlds.resource();

        // Kick players out and close gates
        kickPlayers(original);
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

            Bukkit.getScheduler().runTask(Crown.inst(), () -> {
                // Re-create world
                WorldReCreator creator = WorldReCreator.of(original)
                        .seed(seed)
                        .preserveGameRules(true)
                        .preserveWorldBorder(true);

                World newWorld = creator.run();
                WorldBorder border = VanillaAccess.getLevel(newWorld).getWorldBorder();
                border.setSize(getSize());

                lastSeed = newWorld.getSeed();

                WorldLoader.loadAsync(newWorld).whenComplete(this::onWorldLoaded);
            });
        });
    }

    private void onWorldLoaded(World world, Throwable throwable) {
        if (throwable != null) {
            StaffChat.send(Component.text("Error while resetting RW, cannot finish"), false);
            LOGGER.error("Could not regen Resource World", throwable);
            FtcDiscord.staffLog(C_RW, "Error while resetting Resource World!");
            return;
        }

        BlockStructure spawn = Registries.STRUCTURES.get(spawnStructure);
        if (spawn == null) {
            LOGGER.error("Cannot find RW spawn, no structure with key {} found", spawnStructure);
            return;
        }

        int y = world.getHighestBlockYAt(0, 0, BUKKIT_HEIGHT_MAP) + 1;

        // Take the size of the spawn structure, divide in half
        // and make the vector negative, set the y to first empty
        // Y block, we've now got a place position for the spawn
        Vector3i placePos = spawn.getSize()
                .shiftRight(1) // divide by 2
                .invert()
                .setY(y);

        StructurePlaceContext context = new StructurePlaceContext(spawn, placePos, BlockPlacer.world(world))
                .setEntityPlacer(EntityPlacer.world(world))
                .placeEntities(true)
                .addEmptyEntityProcessor()
                .addEmptyProcessor();

        // Run rest of the code in sync with the server
        Bukkit.getScheduler().runTask(Crown.inst(), () -> {
            // Place spawn structure
            spawn.place(context);
            LOGGER.info("Placed {} at {}", spawn.key(), context.getDestination());

            Vector3i minUnder = placePos.setY(y - 1);
            Vector3i maxUnder = placePos.invert().setY(y - (WG_SIZE_Y >> 1)).subtract(1, 0, 1);

            // Make sure there are no air blocks under
            // the spawn
            WorldBounds3i underArea = WorldBounds3i.of(world, minUnder, maxUnder);
            Orientable data = (Orientable) Material.STRIPPED_DARK_OAK_WOOD.createBlockData();
            data.setAxis(Axis.X);

            for (Block b : underArea) {
                // Don't replace solid blocks
                if (b.isSolid()) continue;

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

                LOGGER.info("Moved rw -> haz gate to {}", Vector3i.of(gateLocation));
            }

            // Move RW -> Haz gate destination
            if (toRes == null) {
                LOGGER.warn("Could not re-position haz -> rw gate, could not find gate with id {}", toResGate);
            } else {
                // Same deal with the 2 method calls as above
                toRes.addTo(null);
                toRes.addTo(destination);

                LOGGER.info("Moved haz -> rw destination to {}", Vector3i.of(destination));
            }

            // Move portal warp
            Key key = Keys.forthecrown("portal");
            Warp portalWarp = Crown.getWarpManager().get(key);

            if (portalWarp == null) {
                LOGGER.warn("Could not find portal warp, no warp with id {}", key);
            } else {
                portalWarp.setDestination(destination);
            }

            // Redefine WorldGuard region to match spawn bounds
            if (!FtcUtils.isNullOrBlank(worldGuardSpawn)) {
                // Figure out bounds
                BlockVector3 min = BlockVector3.at(placePos.getX() - WG_OVERREACH, y - WG_SIZE_Y, placePos.getZ() - WG_OVERREACH);
                BlockVector3 max = min.abs().withY(FtcUtils.MAX_Y);

                // Get manager and region
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
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

            // Purge the CoreProtect database
            long dif = System.currentTimeMillis() - lastReset;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "co purge r:#" + Worlds.RESOURCE_NAME + "t:" + dif / TimeUtil.DAY_IN_MILLIS + "d");

            lastReset = System.currentTimeMillis();
            LOGGER.info("RW reset finished");

            FtcDiscord.staffLog(C_RW, "Resource World reset finished!");
        });
    }

    private void kickPlayers(World w) {
        Location hazel = FtcUtils.findHazelLocation();

        for (Player p : w.getPlayers()) {
            p.teleport(hazel);
        }
    }

    private void setGatesOpen(boolean open) {
        String status = open ? "open" : "close";
        Gate g = Gate.get(toHazGate);

        if (g != null) {
            if (open) {
                Gates.open(g);
            } else {
                Gates.close(g);
            }
        } else {
            LOGGER.warn("Cannot {} haz -> res gate, no gate with {} ID found", status, toHazGate);
        }

        g = Gate.get(toResGate);

        if (g != null) {
            if (open) {
                Gates.open(g);
            } else {
                Gates.close(g);
            }
        } else {
            LOGGER.warn("Cannot {} res -> haz gate, no gate with {} ID found", status, toHazGate);
        }

        Gate.save();
        LOGGER.info("setGatesOpen set, status: {}", status);
    }

    @Override
    public void onDayChange() {
        if (!isAutoResetEnabled()) return;
        if (!TimeUtil.hasCooldownEnded(FtcVars.resourceWorldResetInterval.get(), lastReset)) return;

        if(WorldLoader.isLoading(Worlds.end())) {
            LOGGER.warn("End is already regenerating, moving RW reset ahead by one day");
            return;
        }

        resetAndLoad();
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        this.autoResetEnabled = json.getBool("enabled");
        this.lastReset = json.getLong("last_reset");
        setSize(json.getInt("size"));

        legalSeeds.clear();
        if (json.has("legal_seeds")) {
            this.legalSeeds.addAll(json.getList("legal_seeds", JsonElement::getAsLong, new ArrayList<>()));
        }

        this.spawnStructure = json.getKey("spawn_structure");

        this.lastSeed = json.getLong("last_seed");

        this.toHazGate = json.getString("rw_to_haz_gate");
        this.toResGate = json.getString("haz_to_rw_gate");
        this.worldGuardSpawn = json.getString("wg_spawn_name", "rw_spawn");

        this.resetStart = json.getComponent("reset_start_message");
        this.resetEnd = json.getComponent("reset_end_message");
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("enabled", autoResetEnabled);
        json.add("last_reset", lastReset);
        json.add("size", size);

        if (!legalSeeds.isEmpty()) {
            json.addList("legal_seeds", legalSeeds, JsonPrimitive::new);
        }

        json.addKey("spawn_structure", spawnStructure);

        json.add("last_seed", lastSeed);

        json.add("rw_to_haz_gate", toHazGate);
        json.add("haz_to_rw_gate", toResGate);
        json.add("wg_spawn_name", worldGuardSpawn);

        json.addComponent("reset_start_message", resetStart);
        json.addComponent("reset_end_message", resetEnd);

        return json.getSource();
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
                if (legalSeeds.size() == 1) return legalSeeds.getLong(0);

                long result = FtcUtils.RANDOM.pickRandomEntry(legalSeeds);

                // Loop through set seeds until we find one that isn't
                // the last seed we used
                while (result == lastSeed) {
                    result = FtcUtils.RANDOM.pickRandomEntry(legalSeeds);
                }

                return result;
            }

            // Random seed finder
            long seed = FtcUtils.RANDOM.nextLong();
            short safeGuard = 0;

            while (!isAcceptableSeed(seed)) {
                seed = FtcUtils.RANDOM.nextLong();

                safeGuard++;

                if (safeGuard > 1024) {
                    LOGGER.warn("Couldn't find good seed for ResourceWorld, returning unfit one");
                    break;
                }
            }

            LOGGER.info("Took {} loops to find seed", safeGuard);
            return seed;
        });
    }

    private boolean isAcceptableSeed(long seed) {
        if (seed == lastSeed) return false;

        // Create chunk generator for given seed
        ChunkGenerator gen = WorldGenSettings.makeDefaultOverworld(VanillaAccess.getServer().registryAccess(), seed);

        // Ensure the seed has all required biomes
        // As far as I can see, this is also the most intense part
        // of the seed testing in terms of power needed to run it
        if (!hasBiomes(gen)) return false;

        int baseY = gen.getBaseHeight(0, 0, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR);

        // In most cases, if we're above 75, we're in a hilly area
        // Hilly areas are a no no
        if (baseY > MAX_Y) return false;

        // a ~12 block radius in and around the spawn
        // to make sure it has fitting biomes and is
        // generally flat enough
        for (int x = 0; x < SPAWN_CHECK_QUART; x++) {
            for (int z = 0; z < SPAWN_CHECK_QUART; z++) {
                if (!isAreaGood(x, z, gen, baseY)) return false;
            }
        }

        return true;
    }

    private boolean isAreaGood(int x, int z, ChunkGenerator gen, int baseY) {
        // Biome's use their own positioning,
        // which is 1/4 the size of a chunk
        Holder<Biome> b = gen.getNoiseBiome(x, QuartPos.fromBlock(baseY), z);
        Biome.BiomeCategory category = Biome.getBiomeCategory(b);

        int blockX = x * 4;
        int blockZ = z * 4;

        // Get the difference between this area's
        // Y level and the base Y
        int y = gen.getBaseHeight(blockX, blockZ, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR);
        int dif = baseY - y;

        // For this to return true, the biome must be from
        // an acceptable category as well as having a height
        // difference from the base less than MAX_Y_DIF
        return LEGAL_CATEGORIES.contains(category)
                && dif < MAX_Y_DIF
                && dif > -MAX_Y_DIF;
    }

    private boolean hasBiomes(ChunkGenerator gen) {
        int halfSize = size >> 1; // All my homies hate dividing by 2, bit shifting to the right is where it's at

        EnumSet<Biome.BiomeCategory> biomes = findBiomes(gen, halfSize, QuartPos.fromBlock(64));

        return biomes.containsAll(REQUIRED_CATEGORIES);
    }

    private EnumSet<Biome.BiomeCategory> findBiomes(ChunkGenerator gen, int halfSize, int y) {
        int max = QuartPos.fromBlock(halfSize);
        int min = QuartPos.fromBlock(-halfSize);

        EnumSet<Biome.BiomeCategory> result = EnumSet.noneOf(Biome.BiomeCategory.class);

        // Go through the world area and find the biome at every
        // cord. For the sake of speed, it only gets every 8th biome.
        // Going through each biome might mean as many as 67,000
        // iterations which is not doable lol, this is more like 8,000
        // which is still a lot, but more manageable than not skipping
        // every 8 chunks
        for (int x = min; x < max; x += 8) {
            for (int z = min; z < max; z += 8) {
                result.add(Biome.getBiomeCategory(gen.getNoiseBiome(x, y, z)));
            }
        }

        return result;
    }

    public void setSize(int size) {
        this.size = size;

        World w = Worlds.resource();
        w.getWorldBorder().setSize(size);
    }
}