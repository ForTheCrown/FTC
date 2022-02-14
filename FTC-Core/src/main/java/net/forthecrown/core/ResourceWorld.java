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
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.structure.BlockPlacer;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.EntityPlacer;
import net.forthecrown.structure.StructurePlaceContext;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.utils.Bukkit2NMS;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
import org.bukkit.craftbukkit.v1_18_R1.CraftHeightMap;
import org.bukkit.entity.Player;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Gates;

import java.util.ArrayList;
import java.util.EnumSet;

public class ResourceWorld extends FtcConfig.ConfigSection implements DayChangeListener {
    public static final int
            MAX_Y        = 75,
            WG_OVERREACH = 5,   // The amount of WG region is bigger than the spawn
            WG_SIZE_Y    = 20,  // The amount the WG region is bigger than the spawn, on the Y axis, goes from MAX_Y to spawn.y - WG_SIZE_Y
            MAX_Y_DIF    = 2;   // The max Y difference a potential spawn position can have

    // All legal biome categories that spawn can be in
    public static final EnumSet<Biome.BiomeCategory> LEGAL_CATEGORIES = EnumSet.of(
            Biome.BiomeCategory.PLAINS,
            Biome.BiomeCategory.DESERT
    );

    // The height maps for NMS and Bukkit that are used
    public static final Heightmap.Types HEIGHT_MAP_TYPE = Heightmap.Types.OCEAN_FLOOR_WG;
    public static final HeightMap BUKKIT_HEIGHT_MAP = CraftHeightMap.fromNMS(HEIGHT_MAP_TYPE);

    // An accessor that ChunkGenerator needs for a getBaseHeight call
    public static final LevelHeightAccessor HEIGHT_ACCESSOR = LevelHeightAccessor.create(FtcUtils.MIN_Y, FtcUtils.Y_SIZE);

    private static final Logger LOGGER = Crown.logger();

    private final LongList legalSeeds = new LongArrayList();
    private String toHazGate, toResGate, worldGuardSpawn;
    private Component resetStart, resetEnd;
    private long lastReset;
    private long lastSeed;
    private int size;
    private boolean autoResetEnabled;
    private Key spawnStructure;

    ResourceWorld() {
        super("resource_world");

        Crown.getDayChange().addListener(this);
    }

    public void resetAndLoad() {
        if(!Registries.STRUCTURES.contains(spawnStructure)) {
            LOGGER.error("Cannot start RW reset, no spawn structure with id '{}' found", spawnStructure);
            return;
        }

        LOGGER.info("Starting RW reset");

        World original = Worlds.resource();

        // Kick players out and close gates
        kickPlayers(original);
        setGatesStatus(false);

        // Attempt to announce closing
        if(resetStart == null) {
            LOGGER.warn("resetStart message is null, cannot announce");
        } else {
            Crown.getAnnouncer().announce(resetStart);
        }

        // Re-create world
        WorldReCreator creator = WorldReCreator.of(original)
                .seed(findSeed())
                .preserveGameRules(true)
                .preserveWorldBorder(true);

        World newWorld = creator.run();
        WorldBorder border = Bukkit2NMS.getLevel(newWorld).getWorldBorder();
        border.setSize(getSize());

        lastSeed = newWorld.getSeed();

        WorldLoader.loadAsync(newWorld)
                .whenComplete((world, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Could not regen Resource World", throwable);
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
                        FtcBoundingBox underArea = FtcBoundingBox.of(world, minUnder, maxUnder);
                        Orientable data = (Orientable) Material.STRIPPED_DARK_OAK_WOOD.createBlockData();
                        data.setAxis(Axis.X);

                        for (Block b: underArea) {
                            // Don't replace solid blocks
                            if(b.isSolid()) continue;

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
                            LOGGER.warn("Could not re-position rw -> haz gate, could not find gate with id {}", toResGate);
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
                        if(!FtcUtils.isNullOrBlank(worldGuardSpawn)) {
                            // Figure out bounds
                            BlockVector3 min = BlockVector3.at(placePos.getX() - WG_OVERREACH, y - WG_SIZE_Y, placePos.getZ() - WG_OVERREACH);
                            BlockVector3 max = min.abs().withY(FtcUtils.MAX_Y);

                            // Get manager and region
                            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
                            ProtectedRegion region = manager.getRegion(worldGuardSpawn);

                            // Region might be null, idk how that might happen but still, just in case
                            if(region == null) {
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
                            LOGGER.info("wgSpawnName in resourceWorld is null, cannot edit spawn region");
                        }

                        // Attempt to announce the end's reset being finished
                        if (resetEnd == null) {
                            LOGGER.warn("resetEnd message is null, cannot announce opening");
                        } else {
                            Crown.getAnnouncer().announce(resetEnd);
                        }

                        // Open gates
                        setGatesStatus(true);
                        lastReset = System.currentTimeMillis();

                        LOGGER.info("RW reset finished");
                    });
                });
    }

    private void kickPlayers(World w) {
        Location hazel = FtcUtils.findHazelLocation();

        for (Player p: w.getPlayers()) {
            p.teleport(hazel);
        }
    }

    private void setGatesStatus(boolean open) {
        String status = open ? "open" : "close";
        Gate g = Gate.get(toHazGate);

        if(g != null) {
            if(open) Gates.open(g);
            else Gates.close(g);
        } else {
            LOGGER.warn("Cannot {} haz -> res gate, no gate with {} ID found", status, toHazGate);
        }

        g = Gate.get(toResGate);

        if(g != null) {
            if(open) Gates.open(g);
            else Gates.close(g);
        } else {
            LOGGER.warn("Cannot {} res -> haz gate, no gate with {} ID found", status, toHazGate);
        }

        Gate.save();
        LOGGER.info("setGatesStatus set, status: {}", status);
    }

    @Override
    public void onDayChange() {
        if(!isAutoResetEnabled()) return;
        if (!TimeUtil.hasCooldownEnded(ComVars.resourceWorldResetInterval(), lastSeed)) return;

        resetAndLoad();
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        this.autoResetEnabled = json.getBool("enabled");
        this.lastReset = json.getLong("last_reset");
        setSize(json.getInt("size"));

        legalSeeds.clear();
        if(json.has("legal_seeds")) {
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

        if(!legalSeeds.isEmpty()) {
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

    private long findSeed() {
        if (!legalSeeds.isEmpty()) {
            if(legalSeeds.size() == 1) return legalSeeds.getLong(0);

            long result = FtcUtils.RANDOM.pickRandomEntry(legalSeeds);

            while (result == lastSeed) {
                result = FtcUtils.RANDOM.pickRandomEntry(legalSeeds);
            }

            return result;
        }

        long seed = FtcUtils.RANDOM.nextLong();
        short safeGuard = 0;

        while (!goodSeed(seed)) {
            seed = FtcUtils.RANDOM.nextLong();

            safeGuard++;

            if(safeGuard > 300) {
                LOGGER.warn("Couldn't find good seed for ResourceWorld, returning unfit one");
                break;
            }
        }

        LOGGER.info("Took {} loops to find seed", safeGuard);
        return seed;
    }

    private boolean goodSeed(long seed) {
        if(seed == lastSeed) return false;

        // I... I have no clue
        // This is trying to find a biome at a position in a given seed
        // to determine if it's a good seed or not
        ChunkGenerator gen = WorldGenSettings.makeDefaultOverworld(Bukkit2NMS.getServer().registryAccess(), seed);

        int baseY =  gen.getBaseHeight(0, 0, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR);

        // In most cases, if we're above 75, we're in a hilly area
        // Hilly areas are a no no
        if(baseY > MAX_Y) return false;

        // I think biome checks might be quite expensive
        // so we need to do as few of them as possible,
        // so we check only the surrounding biomes and
        // not the biome where spawn will be
        return isAreaGood( 1,  0, gen, baseY)
                && isAreaGood(-1,  0, gen, baseY)
                && isAreaGood( 0,  1, gen, baseY)
                && isAreaGood( 0, -1, gen, baseY);
    }

    private boolean isAreaGood(int x, int z, ChunkGenerator gen, int baseY) {
        // Biome's use their own positioning,
        // which is 1/4 the size of a chunk
        Biome b = gen.getNoiseBiome(x, QuartPos.fromBlock(64), z);
        Biome.BiomeCategory category = b.getBiomeCategory();

        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);

        // Get the difference between this area's
        // Y level and the base Y
        int y = gen.getBaseHeight(blockX, blockZ, HEIGHT_MAP_TYPE, HEIGHT_ACCESSOR);
        int dif = baseY - y;

        // For this to return true, the biome must be from
        // an acceptable category as well as having a height
        // difference from the base less than MAX_Y_DIF
        return LEGAL_CATEGORIES.contains(category)
                && dif < MAX_Y_DIF;
    }

    public Component getResetStart() {
        return resetStart;
    }

    public void setResetStart(Component resetStart) {
        this.resetStart = resetStart;
    }

    public Component getResetEnd() {
        return resetEnd;
    }

    public void setResetEnd(Component resetEnd) {
        this.resetEnd = resetEnd;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;

        World w = Worlds.resource();
        w.getWorldBorder().setSize(size);
    }

    public boolean isAutoResetEnabled() {
        return autoResetEnabled;
    }

    public void setAutoResetEnabled(boolean autoResetEnabled) {
        this.autoResetEnabled = autoResetEnabled;
    }

    public long getLastReset() {
        return lastReset;
    }

    public LongList getLegalSeeds() {
        return legalSeeds;
    }

    public String getToHazGate() {
        return toHazGate;
    }

    public void setToHazGate(String toHazGate) {
        this.toHazGate = toHazGate;
    }

    public String getToResGate() {
        return toResGate;
    }

    public void setToResGate(String toResGate) {
        this.toResGate = toResGate;
    }

    public Key getSpawnStructure() {
        return spawnStructure;
    }

    public void setSpawnStructure(Key spawnStructure) {
        this.spawnStructure = spawnStructure;
    }

    public String getWorldGuardSpawn() {
        return worldGuardSpawn;
    }

    public void setWorldGuardSpawn(String wgSpawnName) {
        this.worldGuardSpawn = wgSpawnName;
    }
}
