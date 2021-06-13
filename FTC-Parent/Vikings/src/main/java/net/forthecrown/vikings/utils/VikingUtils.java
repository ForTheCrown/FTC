package net.forthecrown.vikings.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Map;
import java.util.function.BiFunction;

public final class VikingUtils {

    public static CrownBoundingBox parseRegion(StringReader reader, CommandSource source) throws CommandSyntaxException {
        PositionArgument posArg = PositionArgument.position();

        Location pos1 = posArg.parse(reader).getBlockLocation(source);
        reader.expect(' ');
        Location pos2 = posArg.parse(reader).getBlockLocation(source);

        return CrownBoundingBox.of(pos1, pos2);
    }

    public static <K, V> JsonArray mapToArray(Map<K, V> map, BiFunction<K, V, JsonElement> function){
        JsonArray array = new JsonArray();

        for (Map.Entry<K, V> e: map.entrySet()){
            array.add(function.apply(e.getKey(), e.getValue()));
        }

        return array;
    }

    public static JsonObject serializeBukkitRegion(BoundingBox box){
        JsonObject json = new JsonObject();

        json.addProperty("minX", box.getMinX());
        json.addProperty("minY", box.getMinY());
        json.addProperty("minZ", box.getMinZ());

        json.addProperty("maxX", box.getMaxX());
        json.addProperty("maxY", box.getMaxY());
        json.addProperty("maxZ", box.getMaxZ());

        return json;
    }

    public static BoundingBox deserializeBukkitRegion(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        double minX = json.get("minX").getAsDouble();
        double minY = json.get("minY").getAsDouble();
        double minZ = json.get("minZ").getAsDouble();

        double maxX = json.get("maxX").getAsDouble();
        double maxY = json.get("maxY").getAsDouble();
        double maxZ = json.get("maxZ").getAsDouble();

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
