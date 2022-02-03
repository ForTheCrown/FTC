package net.forthecrown.regions;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.serializer.AbstractNbtSerializer;
import net.forthecrown.utils.FtcUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.dynmap.markers.Marker;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FtcRegionManager extends AbstractNbtSerializer implements RegionManager {
    private final World world;
    private final FtcRegionPoleGenerator generator;

    private final Map<String, PopulationRegion> byName = new Object2ObjectOpenHashMap<>();
    private final Map<RegionPos, PopulationRegion> byCords = new Object2ObjectOpenHashMap<>();


    public FtcRegionManager(World world) {
        super("regions_" + world.getName());

        this.world = world;

        generator = new FtcRegionPoleGenerator(this);

        reload();
        Crown.logger().info(world.getName() + " region manager loaded");
    }

    @Override
    public void save(CompoundTag tag) {
        //Save all regions
        for (Map.Entry<RegionPos, PopulationRegion> e: byCords.entrySet()) {
            String key = e.getKey().toString();
            Tag serialized = e.getValue().save();

            //If region should be serialized, serialize
            if(serialized != null) tag.put(key, serialized);
        }

        //Remove all unimportant regions
        dropUnimportantRegions();
    }

    @Override
    protected void reload(CompoundTag tag) {
        //Clear em all
        byCords.clear();
        byName.clear();

        for (Map.Entry<String, Tag> e: tag.tags.entrySet()) {
            //Get region pos
            RegionPos pos = RegionPos.fromString(e.getKey());

            //Deserialize region
            PopulationRegion region = new PopulationRegion(pos, world, e.getValue());

            //Add the region
            add(region);
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public PopulationRegion get(RegionPos cords) {
        //Get a region by the given cords or add and then get the new addition
        return byCords.computeIfAbsent(cords, c -> {
            PopulationRegion region = new PopulationRegion(c, world);
            region.updatePoleBounds();

            return region;
        });
    }

    @Override
    public PopulationRegion get(String name) {
        return byName.get(name.toLowerCase());
    }

    @Override
    public RegionData getData(RegionPos pos) {
        PopulationRegion region = byCords.get(pos);
        if(region != null) return region;

        return new RegionData.Empty(pos);
    }

    @Override
    public void rename(PopulationRegion region, String newName, boolean special) {
        region.setProperty(RegionProperty.PAID_REGION, special);

        boolean hasName = region.hasName();
        boolean nullNew = FtcUtils.isNullOrBlank(newName);

        String oldName = region.getName();
        region.setName(newName);

        //Remove it from name tracker if it already has
        if(hasName) {
            byName.remove(oldName.toLowerCase());

            if(nullNew && !region.hasProperty(RegionProperty.FORBIDS_MARKER)) {
                FtcDynmap.getMarker(region).deleteMarker();
            }
        }

        if(!hasName && !nullNew) {
            FtcDynmap.createMarker(region);
        }

        //Set the name and add it with the new name to the tracker
        if(!nullNew) {
            byName.put(newName.toLowerCase(), region);

            if(!region.hasProperty(RegionProperty.FORBIDS_MARKER)) {
                Marker marker = FtcDynmap.getMarker(region);
                if(marker == null) marker = FtcDynmap.createMarker(region);

                marker.setLabel(newName);
            }
        }

        getGenerator().generate(region);

        //Update surrounding regions
        for (int i = 0; i < 4; i++) {
            BlockFace face = BlockFace.values()[i];
            PopulationRegion update = get(region.getPos().add(face.getModX(), face.getModZ()));

            getGenerator().generate(update);
        }

        //Drop any possibly unimportant regions we might've just created
        dropUnimportantRegions();
    }

    @Override
    public void add(PopulationRegion region) {
        //If has name, add to name tracker, always add to cord tracker
        if(region.hasName()) byName.put(region.getName().toLowerCase(), region);
        byCords.put(region.getPos(), region);
    }

    @Override
    public void remove(PopulationRegion region) {
        //Removes the region
        if(region.hasName()) byName.remove(region.getName().toLowerCase());
        byCords.remove(region.getPos());
    }

    @Override
    public void reset(PopulationRegion region) {
        //Remove it
        remove(region);

        //Reset name and pole position
        region.setName(null);
        region.setPolePosition(null);
    }

    @Override
    public Set<String> getRegionNames() {
        return byName.keySet();
    }

    @Override
    public Collection<PopulationRegion> getNamedRegions() {
        return byName.values();
    }

    @Override
    public void dropUnimportantRegions() {
        byCords.entrySet().removeIf(e -> !e.getValue().shouldSerialize());
    }

    @Override
    public RegionPoleGenerator getGenerator() {
        return generator;
    }
}