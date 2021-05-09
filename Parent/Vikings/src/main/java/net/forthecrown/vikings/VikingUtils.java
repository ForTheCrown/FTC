package net.forthecrown.vikings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.utils.JsonUtils;
import net.forthecrown.core.utils.Pair;
import net.forthecrown.vikings.valhalla.creation.ChestGroup;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class VikingUtils {

    public static byte findBiggestInArray(byte[] array){
        byte biggest = array[0];
        for (byte b: array){
            if(b > biggest) biggest = b;
        }
        return biggest;
    }

    public static JsonObject serializeSpawn(NBT nbt, Location location){
        JsonObject result = new JsonObject();

        result.add("nbt", new JsonPrimitive(nbt.serialize()));
        result.add("location", JsonUtils.serializeLocation(location));

        return result;
    }

    public static Pair<Location, NBT> deserializeSpawn(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        Location loc = JsonUtils.deserializeLocation(json.getAsJsonObject("location"));
        NBT nbt = NBT.fromJson(json.get("nbt"));

        return new Pair<>(loc, nbt);
    }

    public static JsonArray serializeLocList(List<Location> locs){
        JsonArray array = new JsonArray();
        for (Location l: locs){
            array.add(JsonUtils.serializeLocation(l));
        }

        return array;
    }

    public static JsonArray serializeChestAreas(List<ChestGroup> groups){
        JsonArray array = new JsonArray();
        groups.forEach(g -> array.add(g.serialize()));

        return array;
    }

    public static JsonArray serializeSpawnMap(Map<Location, NBT> spawns){
        JsonArray array = new JsonArray();
        for (Map.Entry<Location, NBT> s: spawns.entrySet()){
            array.add(serializeSpawn(s.getValue(), s.getKey()));
        }

        return array;
    }
}