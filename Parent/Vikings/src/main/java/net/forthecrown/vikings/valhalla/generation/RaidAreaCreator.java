package net.forthecrown.vikings.valhalla.generation;

import com.google.gson.JsonObject;
import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.core.utils.MapUtils;
import net.forthecrown.vikings.VikingBuilds;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Structure;
import org.bukkit.block.structure.UsageMode;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.forthecrown.core.utils.JsonUtils.deserializeBoundingBox;
import static net.forthecrown.core.utils.JsonUtils.serializeBoundingBox;

//Class existence reason: Make raid area
public class RaidAreaCreator implements JsonSerializable {

    private final VikingRaid raid;
    private CrownBoundingBox region;

    private Map<Location, NBTTagCompound> enemies = new HashMap<>();
    private Map<Location, NBTTagCompound> passives = new HashMap<>();
    private Map<Location, NBTTagCompound> specials = new HashMap<>();

    public RaidAreaCreator(VikingRaid raid) {
        this.raid = raid;
    }

    public RaidAreaCreator(VikingRaid raid, JsonObject json){
        this.raid = raid;

        region = deserializeBoundingBox(json.get("region").getAsJsonObject());
    }

    public void create(){
        Vikings.getRaidManager().getLoader().createWorld();

        activateStructureBlocks();

        spawn(enemies);
        spawn(passives);
        if(raid.currentParty.specialsAllowed) spawn(specials);
    }

    private void activateStructureBlocks(){
        List<Block> strutBlocks = region.getBlocks(block -> block.getType() == Material.STRUCTURE_BLOCK);

        for (Block b: strutBlocks){
            Structure structure = (Structure) b.getState();
            structure.setUsageMode(UsageMode.LOAD);

            //replace placeholders
            structure.setStructureName(VikingBuilds.replacePlaceholders(structure.getStructureName()));
            structure.update();

            //load
            NmsStruct.of(b).a(((CraftWorld) b.getWorld()).getHandle(), true);

            //activate
            Block below = b.getLocation().subtract(0, 1, 0).getBlock();
            Material before = below.getType();
            below.setType(Material.REDSTONE_BLOCK);
            below.setType(before);
        }
    }

    public void spawn(Map<Location, NBTTagCompound> mobs){
        if(MapUtils.isNullOrEmpty(mobs)) return;

        for (Map.Entry<Location, NBTTagCompound> entry: mobs.entrySet()){
            Validate.notNull(entry.getKey());
            Validate.notNull(entry.getValue());

            Location loc = entry.getKey();
            WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();

            NBTTagCompound nmsTag = entry.getValue();

            EntityTypes.a(nmsTag, nmsWorld, ent ->{
                ent.g(loc.getX(), loc.getY(), loc.getZ());
                return ent;
            });
        }
    }

    public RaidAreaCreator setRegion(CrownBoundingBox region) {
        this.region = region;
        return this;
    }

    public RaidAreaCreator setEnemies(Map<Location, NBTTagCompound> enemies) {
        this.enemies = enemies;
        return this;
    }

    public RaidAreaCreator addEnemy(Location spawnLoc, NBTTagCompound tags){
        enemies.put(spawnLoc, tags);
        return this;
    }

    public RaidAreaCreator removeEnemy(Location location){
        enemies.remove(location);
        return this;
    }

    public RaidAreaCreator setPassives(Map<Location, NBTTagCompound> passives) {
        this.passives = passives;
        return this;
    }

    public RaidAreaCreator addPassive(Location spawnLoc, NBTTagCompound tags){
        passives.put(spawnLoc, tags);
        return this;
    }

    public RaidAreaCreator removePassive(Location location){
        passives.remove(location);
        return this;
    }

    public RaidAreaCreator setSpecials(Map<Location, NBTTagCompound> specials) {
        this.specials = specials;
        return this;
    }

    public RaidAreaCreator addSpecial(Location spawnLoc, NBTTagCompound tags){
        specials.put(spawnLoc, tags);
        return this;
    }

    public RaidAreaCreator removeSpecial(Location location){
        specials.remove(location);
        return this;
    }

    public Map<Location, NBTTagCompound> getEnemies() {
        return enemies;
    }

    public Map<Location, NBTTagCompound> getPassives() {
        return passives;
    }

    public Map<Location, NBTTagCompound> getSpecials() {
        return specials;
    }

    @Override
    public JsonObject serialize() {
        JsonObject result = new JsonObject();

        result.add("region", serializeBoundingBox(region));

        return result;
    }
}
