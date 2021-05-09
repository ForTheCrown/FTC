package net.forthecrown.vikings.valhalla.creation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.core.utils.CrownRandom;
import net.forthecrown.core.utils.MapUtils;
import net.forthecrown.vikings.valhalla.VikingRaid;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Structure;

import java.util.List;
import java.util.Map;

import static net.forthecrown.vikings.VikingUtils.*;

public class RaidGenerator implements JsonSerializable {

    private final VikingRaid raid;
    private final CrownRandom random;

    public Map<Location, NBT> hostile_spawns;
    public Map<Location, NBT> passive_spawns;
    public Map<Location, NBT> special_spawns;

    public List<Location> cmd_blocks;
    public List<Location> struct_blocks;

    public List<ChestGroup> chest_groups;

    public RaidGenerator(JsonElement element, VikingRaid raid){
        this.raid = raid;
        this.random = new CrownRandom();
    }

    public RaidGenerator(VikingRaid raid){
        this.raid = raid;
        this.random = new CrownRandom();
    }

    public void generate(){}

    public void spawnHostiles(){ spawns(hostile_spawns); }
    public void spawnPassives(){ spawns(passive_spawns); }
    public void spawnSpecials(){ spawns(special_spawns); }

    public void spawns(Map<Location, NBT> spawns){
        if(MapUtils.isNullOrEmpty(spawns)) return;

        for (Map.Entry<Location, NBT> e: spawns.entrySet()){
            //spawn
        }
    }

    private CrownBoundingBox checkRegionNotNull(){
        if(raid.getRegion() == null) throw new IllegalStateException("Raid area doesn't have a region");
        return raid.getRegion();
    }

    public void activateStructureBlocks(CrownBoundingBox region){

        for (Block b: region.getBlocks(b -> b.getState() instanceof Structure)){
            //Activate blocks
        }
    }

    public void activateCommandBlocks(CrownBoundingBox region){
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("special_spawns", serializeSpawnMap(special_spawns));
        json.add("passive_spawns", serializeSpawnMap(passive_spawns));
        json.add("hostile_spawns", serializeSpawnMap(hostile_spawns));

        json.add("chest_groups", serializeChestAreas(chest_groups));

        json.add("struct_blocks", serializeLocList(struct_blocks));
        json.add("cmd_blocks", serializeLocList(cmd_blocks));

        return json;
    }

    public VikingRaid getRaid() {
        return raid;
    }
}
