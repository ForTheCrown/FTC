package net.forthecrown.utils;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.utils.math.CrownBoundingBox;
import net.minecraft.nbt.TagParser;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class JsonUtils {
    public static JsonElement writeLocation(Location location){
        JsonObject result = new JsonObject();
        result.addProperty("world", location.getWorld().getName());

        result.addProperty("x", location.getX());
        result.addProperty("y", location.getY());
        result.addProperty("z", location.getZ());

        result.addProperty("pitch", location.getPitch());
        result.addProperty("yaw", location.getYaw());

        return result;
    }

    public static Location readLocation(JsonObject jsonObject){
        World world = Objects.requireNonNull(Bukkit.getWorld(jsonObject.get("world").getAsString()));
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();

        float pitch = jsonObject.get("pitch").getAsFloat();
        float yaw = jsonObject.get("yaw").getAsFloat();

        return new Location(world, x, y, z, pitch, yaw);
    }

    public static JsonObject writeRegion(CrownBoundingBox box){
        JsonObject json = new JsonObject();

        json.addProperty("world", box.getWorld().getName());

        json.addProperty("minX", box.getMinX());
        json.addProperty("minY", box.getMinY());
        json.addProperty("minZ", box.getMinZ());

        json.addProperty("maxX", box.getMaxX());
        json.addProperty("maxY", box.getMaxY());
        json.addProperty("maxZ", box.getMaxZ());

        return json;
    }

    public static CrownBoundingBox readRegion(JsonObject json){
        World world = Objects.requireNonNull(Bukkit.getWorld(json.get("world").getAsString()));

        double minX = json.get("minX").getAsDouble();
        double minY = json.get("minY").getAsDouble();
        double minZ = json.get("minZ").getAsDouble();
        double maxX = json.get("maxX").getAsDouble();
        double maxY = json.get("maxY").getAsDouble();
        double maxZ = json.get("maxZ").getAsDouble();

        return new CrownBoundingBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static <T> JsonArray writeCollection(@NotNull Collection<T> collection, @NotNull Function<T, JsonElement> converter){
        Validate.notNull(collection, "Collection was null");
        Validate.notNull(converter, "Converter was null");

        JsonArray result = new JsonArray();

        for (T t: collection){
            result.add(converter.apply(t));
        }
        return result;
    }

    public static <K, V> JsonObject writeMap(Map<K, V> map, Function<Map.Entry<K, V>, Pair<String, JsonElement>> function){
        Validate.notNull(map, "Map was null");
        Validate.notNull(function, "Function was null");

        JsonObject json = new JsonObject();

        for (Map.Entry<K, V> e: map.entrySet()){
            Pair<String, JsonElement> element = function.apply(e);
            if(element == null) continue;

            json.add(element.first, element.second);
        }

        return json;
    }

    public static <K, V> Map<K, V> readMap(JsonObject json, Function<Map.Entry<String, JsonElement>, Pair<K, V>> function){
        Map<K, V> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            Pair<K, V> pair = function.apply(e);
            if(pair == null) continue;

            result.put(pair.first, pair.second);
        }

        return result;
    }

    public static <T extends Enum<T>> T readEnum(Class<T> clazz, JsonElement element){
        if(element == null || element.isJsonNull()) return null;
        return Enum.valueOf(clazz, element.getAsString().toUpperCase());
    }

    public static <E extends Enum<E>> JsonElement writeEnum(E anum){
        return anum == null ? JsonNull.INSTANCE : new JsonPrimitive(anum.name().toLowerCase());
    }

    public static ItemStack readItem(JsonElement json) throws CommandSyntaxException {
        return NbtHandler.itemFromNBT(NBT.of(TagParser.parseTag(json.getAsString())));
    }

    public static JsonElement writeItem(ItemStack itemStack){
        return new JsonPrimitive(NbtHandler.ofItem(itemStack).serialize());
    }
}
