package net.forthecrown.utils;

import com.google.common.base.Charsets;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.players.BanListEntry;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
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
            world = Bukkit.getWorld(json.get("world").getAsString());
        }

        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0f;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;

        return new Location(world, x, y, z, yaw, pitch);
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

    //Writes the item using its NBT
    public static JsonPrimitive writeItem(ItemStack itemStack){
        return new JsonPrimitive(CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()).toString());
    }

    public static JsonPrimitive writeKey(Keyed keyed) {
        return writeKey(keyed.key());
    }

    public static JsonPrimitive writeKey(Key key){
        return new JsonPrimitive(key.asString());
    }

    public static NamespacedKey readKey(JsonElement element){
        return Keys.parse(element.getAsString());
    }

    // Read the UUID from the element
    // if element is a number, the ID is stored as a BigInteger
    // if it's a string, it's read as a string representation
    // of the uuid
    public static UUID readUUID(JsonElement element){
        JsonPrimitive primitive = element.getAsJsonPrimitive();

        if(primitive.isNumber()) {
            BigInteger bigInt = primitive.getAsBigInteger();
            return convertFromBigInteger(bigInt);
        } else return UUID.fromString(primitive.getAsString());
    }

    // Writes the UUID as a BigInteger using some code I copied
    // and pasted from Google
    public static JsonPrimitive writeUUID(UUID id){
        return new JsonPrimitive(convertToBigInteger(id));
    }

    private static final BigInteger B = BigInteger.ONE.shiftLeft(64); // 2^64
    private static final BigInteger L = BigInteger.valueOf(Long.MAX_VALUE);

    public static BigInteger convertToBigInteger(UUID id) {
        BigInteger lo = BigInteger.valueOf(id.getLeastSignificantBits());
        BigInteger hi = BigInteger.valueOf(id.getMostSignificantBits());

        // If any of lo/hi parts is negative interpret as unsigned

        if (hi.signum() < 0) hi = hi.add(B);
        if (lo.signum() < 0) lo = lo.add(B);

        return lo.add(hi.multiply(B));
    }

    public static UUID convertFromBigInteger(BigInteger x) {
        BigInteger[] parts = x.divideAndRemainder(B);
        BigInteger hi = parts[0];
        BigInteger lo = parts[1];

        if (L.compareTo(lo) < 0) lo = lo.subtract(B);
        if (L.compareTo(hi) < 0) hi = hi.subtract(B);

        return new UUID(hi.longValueExact(), lo.longValueExact());
    }

    public static int[] readIntArray(JsonArray array) {
        int[] arr = new int[array.size()];

        for (int i = 0; i < array.size(); i++) {
            arr[i] = array.get(i).getAsInt();
        }

        return arr;
    }

    public static JsonArray writeIntArray(int... arr) {
        JsonArray array = new JsonArray();

        for (int j : arr) {
            array.add(j);
        }

        return array;
    }

    private static final SimpleDateFormat DATE_FORMAT = BanListEntry.DATE_FORMAT;
    private static final DateFormat LEGACY_FORMAT = DateFormat.getDateInstance();

    public static Date readDate(JsonElement element) {
        try {
            return DATE_FORMAT.parse(element.getAsString());
        } catch (ParseException e) {
            try {
                return LEGACY_FORMAT.parse(element.getAsString());
            } catch (ParseException e1) {
                return new Date(); // Fuck this horse-shit garbage
                                   // This only works when it feels like working
                                   // So fuck this date parsing bullshit, and
                                   // retardedness that comes with it, why the fuck
                                   // does DateFormat.getInstance() fail here,
                                   // there's literally no reason for it to fail
            }
        }
    }

    public static JsonElement writeDate(Date date) {
        return new JsonPrimitive(DATE_FORMAT.format(date));
    }

    static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static Gson getGSON() {
        return gson;
    }

    //Writes json to a file
    public static void writeFile(JsonElement json, File f) throws IOException {
        FileWriter writer = new FileWriter(f, Charsets.UTF_8);

        JsonWriter jWriter = gson.newJsonWriter(writer);
        gson.toJson(json, jWriter);

        jWriter.close();
        writer.close();
    }

    //Reads json from a file
    public static JsonObject readFileObject(File file) throws IOException {
        return readFile(file).getAsJsonObject();
    }

    public static JsonElement readFile(File file) throws IOException {
        FileReader reader = new FileReader(file, Charsets.UTF_8);
        JsonElement json = JsonParser.parseReader(reader);

        reader.close();

        return json;
    }
}
