package net.forthecrown.utils;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.utils.math.FtcRegion;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.nbt.TagParser;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility methods to make serializing and deserializing json easier
 */
public final class JsonUtils {
    private JsonUtils() {}

    public static JsonObject writeLocation(Location location){
        JsonObject result = new JsonObject();

        if(location.getWorld() != null) result.addProperty("world", location.getWorld().getName());

        result.addProperty("x", location.getX());
        result.addProperty("y", location.getY());
        result.addProperty("z", location.getZ());

        if(location.getPitch() != 0f) result.addProperty("pitch", location.getPitch());
        if(location.getYaw() != 0f) result.addProperty("yaw", location.getYaw());

        return result;
    }

    public static Location readLocation(JsonObject json){
        World world;

        if(json.has("world")){
            World gottenWorld = Bukkit.getWorld(json.get("world").getAsString());
            world = gottenWorld == null ? Worlds.OVERWORLD : gottenWorld;
        } else world = null;

        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;
        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0f;

        return new Location(world, x, y, z, pitch, yaw);
    }

    public static Position readPosition(JsonObject json) {
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        return new PositionImpl(x, y, z);
    }

    public static JsonObject writePosition(Position position) {
        JsonObject json = new JsonObject();

        json.addProperty("x", position.x());
        json.addProperty("y", position.y());
        json.addProperty("z", position.z());

        return json;
    }

    public static JsonObject writeRegion(FtcRegion box){
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

    public static FtcRegion readRegion(JsonObject json){
        World world = Objects.requireNonNull(Bukkit.getWorld(json.get("world").getAsString()));

        double minX = json.get("minX").getAsDouble();
        double minY = json.get("minY").getAsDouble();
        double minZ = json.get("minZ").getAsDouble();

        double maxX = json.get("maxX").getAsDouble();
        double maxY = json.get("maxY").getAsDouble();
        double maxZ = json.get("maxZ").getAsDouble();

        return new FtcRegion(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static JsonObject writeBoundingBox(BoundingBox box) {
        JsonBuf json = JsonBuf.empty();

        json.add("minX", box.getMinX());
        json.add("minY", box.getMinY());
        json.add("minZ", box.getMinZ());

        json.add("maxX", box.getMaxX());
        json.add("maxY", box.getMaxY());
        json.add("maxZ", box.getMaxZ());

        return json.getSource();
    }

    public static BoundingBox readBoundingBox(JsonObject json) {
        double minX = json.get("minX").getAsDouble();
        double minY = json.get("minY").getAsDouble();
        double minZ = json.get("minZ").getAsDouble();

        double maxX = json.get("maxX").getAsDouble();
        double maxY = json.get("maxY").getAsDouble();
        double maxZ = json.get("maxZ").getAsDouble();

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
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

    public static <T> void readList(JsonElement element, Function<JsonElement, T> func, Consumer<T> adder){
        JsonArray array = element.getAsJsonArray();
        array.forEach(e -> adder.accept(func.apply(e)));
    }

    public static <T extends Enum<T>> T readEnum(Class<T> clazz, JsonElement element){
        if(element == null || element.isJsonNull()) return null;
        return Enum.valueOf(clazz, element.getAsString().toUpperCase());
    }

    public static <E extends Enum<E>> JsonElement writeEnum(E anum){
        return anum == null ? JsonNull.INSTANCE : new JsonPrimitive(anum.name().toLowerCase());
    }

    //Reads the item using a string representation of it's NBT data
    public static ItemStack readItem(JsonElement json) {
        try {
            return NbtHandler.itemFromNBT(NBT.of(TagParser.parseTag(json.getAsString())));
        } catch (CommandSyntaxException e){
            e.printStackTrace();
            return null;
        }
    }

    //Writes the item using it's NBT
    public static JsonPrimitive writeItem(ItemStack itemStack){
        return new JsonPrimitive(NbtHandler.ofItem(itemStack).serialize());
    }

    public static JsonPrimitive writeKey(Key key){
        return new JsonPrimitive(key.asString());
    }

    public static Key readKey(JsonElement element){
        return FtcUtils.parseKey(element.getAsString());
    }

    public static UUID readUUID(JsonElement element){
        return UUID.fromString(element.getAsString());
    }

    public static JsonPrimitive writeUUID(UUID id){
        return new JsonPrimitive(id.toString());
    }

    //Writes json to a file
    public static void writeFile(JsonObject json, File f) throws IOException {
        FileWriter writer = new FileWriter(f);
        writer.write(json.toString());
        writer.close();
    }

    //Reads json from a file
    public static JsonObject readFile(File file) throws IOException {
        FileReader reader = new FileReader(file);
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(reader).getAsJsonObject();
        reader.close();

        return json;
    }
}
