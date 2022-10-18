package net.forthecrown.regions;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.core.Worlds;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.dynmap.markers.Marker;

import java.util.Collection;
import java.util.Map;

public class RegionManager extends SerializableObject.NbtDat {
    private static RegionManager inst;

    @Getter
    private final World world;

    private final Map<String, PopulationRegion> byName = new Object2ObjectOpenHashMap<>();
    private final Map<RegionPos, PopulationRegion> byCords = new Object2ObjectOpenHashMap<>();

    public RegionManager(World world) {
        super(PathUtil.pluginPath("regions_" + world.getName() + ".dat"));
        this.world = world;
    }

    public static RegionManager get() {
        return inst;
    }

    static void init() {
        inst = new RegionManager(Worlds.overworld());
        inst.reload();

        AutoSave.get()
                .addCallback(() -> get().save());
    }

    /**
     * Gets a region by it's cords
     * @param cords The cords to get a region by
     * @return The region at the given cords
     */
    public PopulationRegion get(RegionPos cords) {
        //Get a region by the given cords or add and then get the new addition
        return byCords.computeIfAbsent(cords, c -> {
            PopulationRegion region = new PopulationRegion(c);
            region.updatePoleBounds();

            return region;
        });
    }

    /**
     * Gets a region by it's name
     * @param name The name to get a region by
     * @return The region with the given name, or null, if no region exists by the given name
     */
    public PopulationRegion get(String name) {
        return byName.get(name.toLowerCase());
    }

    /**
     * Gets a snapshot of a region
     * @param pos The region position to get the snapshot of
     * @return The region's snapshot, will be {@link PopulationRegion} if
     *         the region is important, {@link RegionAccess.Empty} otherwise
     */
    public RegionAccess getAccess(RegionPos pos) {
        PopulationRegion region = byCords.get(pos);
        if (region != null) {
            return region;
        }

        return new RegionAccess.Empty(pos);
    }

    /**
     * Renames a given region to the given name.
     * @param region The region to rename
     * @param newName The new name the region will have
     * @param special Whether the region was paid for on the web-store
     */
    public void rename(PopulationRegion region, String newName, boolean special) {
        region.setProperty(RegionProperty.PAID_REGION, special);

        boolean hasName = region.hasName();
        boolean nullNew = Util.isNullOrBlank(newName);

        String oldName = region.getName();
        region.setName(newName);

        //Remove it from name tracker if it already has
        if (hasName) {
            byName.remove(oldName.toLowerCase());

            if (nullNew && !region.hasProperty(RegionProperty.FORBIDS_MARKER)) {
                FtcDynmap.getMarker(region).deleteMarker();
            }
        }


        //Set the name and add it with the new name to the tracker
        if (!nullNew) {
            byName.put(newName.toLowerCase(), region);

            if (!region.hasProperty(RegionProperty.FORBIDS_MARKER)) {
                Marker marker = FtcDynmap.createMarker(region);
                marker.setLabel(newName);
            }
        }

        Regions.placePole(region);

        //Update surrounding regions
        for (int i = 0; i < 4; i++) {
            BlockFace face = BlockFace.values()[i];
            PopulationRegion update = get(region.getPos().add(face.getModX(), face.getModZ()));

            Regions.placePole(update);
        }

        //Drop any possibly unimportant regions we might've just created
        dropUnimportantRegions();
    }

    /**
     * Adds a region to the manager
     * @param region The region to add
     */
    public void add(PopulationRegion region) {
        //If has name, add to name tracker, always add to cord tracker
        if(region.hasName()) {
            byName.put(region.getName().toLowerCase(), region);
        }

        byCords.put(region.getPos(), region);
    }

    /**
     * Removes a region from this manager
     * @param region The region to remove
     */
    public void remove(PopulationRegion region) {
        //Removes the region
        if(region.hasName()) byName.remove(region.getName().toLowerCase());
        byCords.remove(region.getPos());
    }

    /**
     * Resets a region
     * <p></p>
     * Warning: resetLand does not currently function
     * @param region The region to reset
     */
    public void reset(PopulationRegion region) {
        //Remove it
        remove(region);

        //Reset name and pole position
        region.setName(null);
        region.setPolePosition(null);
    }

    /**
     * Gets all named regions
     * @return All named regions
     */
    public Collection<PopulationRegion> getNamedRegions() {
        return byName.values();
    }

    /**
     * Removes all 'unimportant' regions from the manager's tracking.
     * <p></p>
     * Note: unimportant refers to the return result of {@link PopulationRegion#isImportant()},
     * If that returns false, the region is considered 'unimportant'
     */
    public void dropUnimportantRegions() {
        byCords.entrySet().removeIf(e -> !e.getValue().isImportant());
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public void save(CompoundTag tag) {
        //Save all regions
        for (Map.Entry<RegionPos, PopulationRegion> e: byCords.entrySet()) {
            String key = e.getKey().toString();
            CompoundTag t = new CompoundTag();
            e.getValue().save(t);

            //If region should be serialized, serialize
            if (t.isEmpty()) {
                continue;
            }

            tag.put(key, t);
        }

        //Remove all unimportant regions
        dropUnimportantRegions();
    }

    @Override
    protected void load(CompoundTag tag) {
        //Clear em all
        byCords.clear();
        byName.clear();

        for (Map.Entry<String, Tag> e: tag.tags.entrySet()) {
            //Get region pos
            RegionPos pos = RegionPos.fromString(e.getKey());

            //Deserialize region
            PopulationRegion region = new PopulationRegion(pos, (CompoundTag) e.getValue());

            //Add the region
            add(region);
        }
    }
}