package net.forthecrown.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.TagParser;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
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

    public static Location readLocation(JsonObject json) {
        World world = null;

        if(json.has("world")) {
            World gottenWorld = Bukkit.getWorld(json.get("world").getAsString());
            world = gottenWorld == null ? Worlds.OVERWORLD : gottenWorld;
        }

        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0f;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static JsonObject writeRegion(FtcBoundingBox box){
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

    public static FtcBoundingBox readRegion(JsonObject json){
        World world = Objects.requireNonNull(Bukkit.getWorld(json.get("world").getAsString()));

        double minX = json.get("minX").getAsDouble();
        double minY = json.get("minY").getAsDouble();
        double minZ = json.get("minZ").getAsDouble();

        double maxX = json.get("maxX").getAsDouble();
        double maxY = json.get("maxY").getAsDouble();
        double maxZ = json.get("maxZ").getAsDouble();

        return new FtcBoundingBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static JsonObject writeBoundingBox(BoundingBox box) {
        JsonWrapper json = JsonWrapper.empty();

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
            return CraftItemStack.asBukkitCopy(
                    net.minecraft.world.item.ItemStack.of(
                            TagParser.parseTag(json.getAsString())
                    )
            );
        } catch (CommandSyntaxException e){
            e.printStackTrace();
            return null;
        }
    }

    //Writes the item using it's NBT
    public static JsonPrimitive writeItem(ItemStack itemStack){
        return new JsonPrimitive(NbtHandler.ofItem(itemStack).serialize());
    }

    public static JsonPrimitive writeKey(Keyed keyed) {
        return writeKey(keyed.key());
    }

    public static JsonPrimitive writeKey(Key key){
        return new JsonPrimitive(key.asString());
    }

    public static Key readKey(JsonElement element){
        return FtcUtils.parseKey(element.getAsString());
    }

    public static UUID readUUID(JsonElement element){
        JsonPrimitive primitive = element.getAsJsonPrimitive();

        if(primitive.isNumber()) {
            BigInteger bigInt = primitive.getAsBigInteger();
            byte[] data = bigInt.toByteArray();

            long msb = 0;
            long lsb = 0;
            assert data.length == 16 : "data must be 16 bytes in length";
            for (int i=0; i<8; i++)  msb = (msb << 8) | (data[i] & 0xff);
            for (int i=8; i<16; i++) lsb = (lsb << 8) | (data[i] & 0xff);

            return new UUID(msb, lsb);
        } else return UUID.fromString(primitive.getAsString());
    }

    private static byte[] toByteArray(UUID uuid) {
        byte[] result = new byte[16];

        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();

        int i = 0;
        result[i]   = (byte)(most >>> 56);
        result[++i] = (byte)(most >>> 48);
        result[++i] = (byte)(most >>> 40);
        result[++i] = (byte)(most >>> 32);
        result[++i] = (byte)(most >>> 24);
        result[++i] = (byte)(most >>> 16);
        result[++i] = (byte)(most >>>  8);
        result[++i] = (byte)(most >>>  0);

        result[++i] = (byte)(least >>> 56);
        result[++i] = (byte)(least >>> 48);
        result[++i] = (byte)(least >>> 40);
        result[++i] = (byte)(least >>> 32);
        result[++i] = (byte)(least >>> 24);
        result[++i] = (byte)(least >>> 16);
        result[++i] = (byte)(least >>>  8);
        result[++i] = (byte)(least >>>  0);

        return result;
    }

    public static JsonPrimitive writeUUID(UUID id){
        return new JsonPrimitive(new BigInteger(1, toByteArray(id)));
    }

    public static JsonObject writeVanillaBoundingBox(net.minecraft.world.level.levelgen.structure.BoundingBox box) {
        JsonWrapper json = JsonWrapper.empty();

        json.add("minX", box.minX());
        json.add("minY", box.minY());
        json.add("minZ", box.minZ());

        json.add("maxX", box.maxX());
        json.add("maxY", box.maxY());
        json.add("maxZ", box.maxZ());

        return json.getSource();
    }

    public static net.minecraft.world.level.levelgen.structure.BoundingBox readVanillaBoundingBox(JsonObject element) {
        JsonWrapper j = JsonWrapper.of(element);

        return new net.minecraft.world.level.levelgen.structure.BoundingBox(
                j.getInt("minX"),
                j.getInt("minY"),
                j.getInt("minZ"),

                j.getInt("maxX"),
                j.getInt("maxY"),
                j.getInt("maxZ")
        );
    }

    static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    //Writes json to a file
    public static void writeFile(JsonElement json, File f) throws IOException {
        FileWriter writer = new FileWriter(f);

        JsonWriter jWriter = gson.newJsonWriter(writer);
        gson.toJson(json, jWriter);

        jWriter.close();
        writer.close();
    }

    //Reads json from a file
    public static JsonObject readFile(File file) throws IOException {
        return readFileElement(file).getAsJsonObject();
    }

    public static JsonElement readFileElement(File file) throws IOException {
        FileReader reader = new FileReader(file);
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(reader);

        reader.close();

        return json;
    }
}
