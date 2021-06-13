package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.utils.BlockPos;
import net.forthecrown.vikings.utils.VikingUtils;
import net.forthecrown.vikings.valhalla.builder.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidDifficulty;
import net.forthecrown.vikings.valhalla.active.RaidParty;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MobData implements RaidData {

    public final Map<BlockPos, NBT> hostileMobs = new HashMap<>();
    public final Map<BlockPos, NBT> passiveMobs = new HashMap<>();
    public final Map<BlockPos, NBT> specialMobs = new HashMap<>();

    public MobData() {
    }

    public MobData(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        specialMobs.putAll(deserializeMobs(json.get("specialMobs")));
        hostileMobs.putAll(deserializeMobs(json.get("hostileMobs")));
        passiveMobs.putAll(deserializeMobs(json.get("passiveMobs")));
    }

    @Override
    public void create(RaidParty party, BattleBuilder generator) {
        if(hasNothingToSpawn()) return;

        spawn(hostileMobs, generator);
        spawn(passiveMobs, generator);
        if(shouldSpawnSpecials(generator)) spawn(specialMobs, generator);
    }

    private void spawn(Map<BlockPos, NBT> map, BattleBuilder builder){
        World world = builder.world;
        WorldServer worldServer = ((CraftWorld) world).getHandle();

        for (Map.Entry<BlockPos, NBT> e: map.entrySet()){
            Location loc = e.getKey().toLoc(world).toCenterLocation();
            NBTTagCompound nbt = e.getValue().getNMS();

            Entity ent = EntityTypes.a(nbt, worldServer, entity -> {
                entity.setPosition(loc.getX(), loc.getY(), loc.getZ());
                return entity;
            });

            worldServer.addAllEntitiesSafely(ent, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
    }

    public boolean hasNothingToSpawn(){
        return hostileMobs.isEmpty() && passiveMobs.isEmpty() && specialMobs.isEmpty();
    }

    public boolean shouldSpawnSpecials(BattleBuilder builder){
        return builder.difficulty.enemyHealthMod >= RaidDifficulty.medium();
    }

    @Override
    public JsonObject serialize() {
        if(hasNothingToSpawn()) return null;

        JsonObject json = new JsonObject();

        if(!specialMobs.isEmpty()) json.add("specialMobs", serializeMobs(specialMobs));
        if(!hostileMobs.isEmpty()) json.add("hostileMobs", serializeMobs(hostileMobs));
        if(!passiveMobs.isEmpty()) json.add("passiveMobs", serializeMobs(passiveMobs));

        return json;
    }

    private JsonArray serializeMobs(Map<BlockPos, NBT> spawns){
        return VikingUtils.mapToArray(spawns,(loc, nbt) -> {
            JsonObject entry = new JsonObject();

            entry.add("location", loc.serialize());
            entry.add("nbt", new JsonPrimitive(nbt.serialize()));

            return entry;
        });
    }

    private Map<BlockPos, NBT> deserializeMobs(JsonElement array){
        if(array == null || !array.isJsonArray()) return Collections.emptyMap();

        Map<BlockPos, NBT> tempMap = new HashMap<>();
        for (JsonElement ele: array.getAsJsonArray()){
            JsonObject json = ele.getAsJsonObject();

            BlockPos pos = BlockPos.of(json.get("location"));
            NBT nbt = NBT.fromJson(json.get("nbt"));

            tempMap.put(pos, nbt);
        }

        return tempMap;
    }
}
