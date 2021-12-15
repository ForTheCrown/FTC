package net.forthecrown.regions;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractNbtSerializer;
import net.forthecrown.utils.FtcUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class FtcRegionManager extends AbstractNbtSerializer implements RegionManager {
    private final World world;
    private final FtcRegionPoleGenerator generator;

    private final Object2ObjectMap<String, PopulationRegion> byName = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<RegionPos, PopulationRegion> byCords = new Object2ObjectOpenHashMap<>();


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
            Tag serialized = e.getValue().saveAsTag();

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
        return byCords.computeIfAbsent(cords, (Function<RegionPos, PopulationRegion>) c -> {
            PopulationRegion region = new PopulationRegion(c, world);
            region.updatePoleBounds();

            return region;
        });
    }

    @Override
    public PopulationRegion get(String name) {
        return byName.get(name);
    }

    @Override
    public RegionData getData(RegionPos pos) {
        PopulationRegion region = byCords.get(pos);
        if(region != null) return region;

        return new RegionData.Empty(pos);
    }

    @Override
    public void rename(PopulationRegion region, String newName) {
        boolean hasName = region.hasName();
        boolean nullNew = FtcUtils.isNullOrBlank(newName);
        //Remove it from name tracker if it already has
        if(hasName) byName.remove(region.getName());

        //Set the name and add it with the new name to the tracker
        region.setName(newName);
        if(!nullNew) byName.put(newName, region);

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
        if(region.hasName()) byName.put(region.getName(), region);
        byCords.put(region.getPos(), region);
    }

    @Override
    public void remove(PopulationRegion region) {
        //Removes the region
        if(region.hasName()) byName.remove(region.getName());
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
        byCords.object2ObjectEntrySet().removeIf(e -> !e.getValue().shouldSerialize());
    }

    @Override
    public RegionPoleGenerator getGenerator() {
        return generator;
    }
}