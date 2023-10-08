package net.forthecrown.utils.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for methods to make serializing various objects into and from JSON easier.
 * <p>
 * I love how verbose GSON is by itself
 */
public final class JsonUtils {
  private JsonUtils() {}

  public static final SimpleDateFormat DATE_FORMAT
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);

  private static final DateFormat LEGACY_FORMAT
      = DateFormat.getDateInstance();

  private static final BigInteger B = BigInteger.ONE.shiftLeft(64); // 2^64
  private static final BigInteger L = BigInteger.valueOf(Long.MAX_VALUE);

  public static final EnumTypeAdapter ENUM_TYPE_ADAPTER = new EnumTypeAdapter();

  public static JsonObject writeLocation(Location location) {
    JsonObject result = new JsonObject();

    if (location.getWorld() != null) {
      result.addProperty("world", location.getWorld().getName());
    }

    result.addProperty("x", location.getX());
    result.addProperty("y", location.getY());
    result.addProperty("z", location.getZ());

    if (location.getPitch() != 0f) {
      result.addProperty("pitch", location.getPitch());
    }

    if (location.getYaw() != 0f) {
      result.addProperty("yaw", location.getYaw());
    }

    return result;
  }

  public static Location readLocation(JsonElement element) {
    JsonObject json = element.getAsJsonObject();

    World world = null;

    if (json.has("world")) {
      world = Bukkit.getWorld(json.get("world").getAsString());
    }

    double x = json.get("x").getAsDouble();
    double y = json.get("y").getAsDouble();
    double z = json.get("z").getAsDouble();

    float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0f;
    float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;

    return new Location(world, x, y, z, yaw, pitch);
  }

  public static <T extends Enum<T>> T readEnum(Class<T> clazz, JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return null;
    }

    return Enum.valueOf(clazz, element.getAsString().toUpperCase());
  }

  public static <E extends Enum<E>> JsonElement writeEnum(E anum) {
    return anum == null ? JsonNull.INSTANCE : new JsonPrimitive(anum.name().toLowerCase());
  }

  public static ItemStack readItem(JsonElement json) {
    return ItemStacks.fromNbtString(json.getAsString());
  }

  public static JsonPrimitive writeItem(ItemStack itemStack) {
    return new JsonPrimitive(ItemStacks.save(itemStack).toString());
  }

  public static JsonPrimitive writeKey(Key key) {
    return new JsonPrimitive(key.asString());
  }

  public static NamespacedKey readKey(JsonElement element) {
    return NamespacedKey.fromString(element.getAsString());
  }

  // Read the UUID from the element
  // if element is a number, the ID is stored as a BigInteger
  // if it's a string, it's read as a string representation
  // of the uuid
  public static UUID readUUID(JsonElement element) {
    JsonPrimitive primitive = element.getAsJsonPrimitive();

    if (primitive.isNumber()) {
      BigInteger bigInt = primitive.getAsBigInteger();
      return convertFromBigInteger(bigInt);
    } else {
      return UUID.fromString(primitive.getAsString());
    }
  }

  public static JsonPrimitive writeUUID(UUID id) {
    return new JsonPrimitive(id.toString());
  }

  public static UUID convertFromBigInteger(BigInteger x) {
    BigInteger[] parts = x.divideAndRemainder(B);
    BigInteger hi = parts[0];
    BigInteger lo = parts[1];

    if (L.compareTo(lo) < 0) {
      lo = lo.subtract(B);
    }
    if (L.compareTo(hi) < 0) {
      hi = hi.subtract(B);
    }

    return new UUID(hi.longValueExact(), lo.longValueExact());
  }

  public static int[] readIntArray(JsonArray array) {
    int[] arr = new int[array.size()];

    for (int i = 0; i < array.size(); i++) {
      arr[i] = array.get(i).getAsInt();
    }

    return arr;
  }

  public static Date readDate(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      return null;
    }

    var prim = element.getAsJsonPrimitive();

    if (prim.isNumber()) {
      return new Date(prim.getAsLong());
    }

    try {
      return DATE_FORMAT.parse(element.getAsString());
    } catch (ParseException e) {
      throw new IllegalStateException(e);
    }
  }

  public static JsonElement writeDate(Date date) {
    try {
      return new JsonPrimitive(DATE_FORMAT.format(date));
    } catch (Throwable t) {
      return new JsonPrimitive(date.getTime());
    }
  }

  public static long readTimestamp(JsonElement element) {
    return readTimestamp(element, -1L);
  }

  public static long readTimestamp(JsonElement element, long def) {
    var date = readDate(element);
    return date == null ? def : date.getTime();
  }

  public static JsonElement writeTimestamp(long time) {
    return writeDate(new Date(time));
  }

  public static Component readText(JsonElement element) {
    if (element instanceof JsonPrimitive primitive && primitive.isString()) {
      return Text.renderString(primitive.getAsString());
    }

    if (element instanceof JsonArray array) {
      var builder = Component.text();
      for (JsonElement jsonElement : array) {
        Component text = readText(jsonElement);
        builder.append(text);
      }
      return builder.build();
    }

    return GsonComponentSerializer.gson()
        .deserializeFromTree(element);
  }

  public static ViewerAwareMessage readMessage(JsonElement element) {
    if (element.isJsonPrimitive()) {
      String str = element.getAsString();
      return viewer -> Text.valueOf(str, viewer);
    }

    // If is PlayerMessage
    if (element.isJsonObject()) {
      var obj = element.getAsJsonObject();

      if (obj.has("message")) {
        return PlayerMessage.load(new Dynamic<>(JsonOps.INSTANCE, element))
            .getOrThrow(false, s -> {});
      }
    }

    Component base = readText(element);
    return ViewerAwareMessage.wrap(base);
  }

  public static JsonElement writeText(Component component) {
    return GsonComponentSerializer.gson()
        .serializeToTree(component);
  }

  public static Duration readDuration(JsonElement element) {
    return FtcCodecs.DURATION.decode(new Dynamic<>(JsonOps.INSTANCE, element))
        .map(Pair::getFirst)
        .getOrThrow(false, string -> {});
  }

  static final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .create();

  public static void writeFile(JsonElement json, Path f) throws IOException {
    PathUtil.ensureParentExists(f);
    var writer = Files.newBufferedWriter(f, StandardCharsets.UTF_8);

    JsonWriter jWriter = gson.newJsonWriter(writer);
    gson.toJson(json, jWriter);

    jWriter.close();
    writer.close();
  }

  public static JsonObject readFileObject(Path file) throws IOException {
    return readFile(file).getAsJsonObject();
  }

  public static JsonElement readFile(Path file) throws IOException {
    var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
    JsonElement json = JsonParser.parseReader(reader);
    reader.close();
    return json;
  }

  public static <T> TypeAdapter<T> createAdapter(
      Function<T, JsonElement> serializer,
      Function<JsonElement, T> deserializer
  ) {
    return new TypeAdapter<>() {
      @Override
      public void write(JsonWriter out, T value) throws IOException {
        var element = serializer.apply(value);
        TypeAdapters.JSON_ELEMENT.write(out, element);
      }

      @Override
      public T read(JsonReader in) throws IOException {
        var element = TypeAdapters.JSON_ELEMENT.read(in);
        return deserializer.apply(element);
      }
    };
  }

  public static Stream<JsonElement> stream(JsonArray array) {
    return StreamSupport.stream(array.spliterator(), false);
  }

  public static JsonArray ofStream(Stream<JsonElement> stream) {
    return (JsonArray) JsonOps.INSTANCE.createList(stream);
  }

  public static Instant readInstant(JsonElement element) {
    long timestamp = readTimestamp(element, -1);

    if (timestamp == -1) {
      return null;
    }

    return Instant.ofEpochMilli(timestamp);
  }

  public static JsonElement writeInstant(Instant instant) {
    return writeTimestamp(instant.toEpochMilli());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static class EnumTypeAdapter implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!type.getRawType().isEnum()) {
        return null;
      }

      Class<? extends Enum> eClass = (Class<? extends Enum>) type.getRawType();

      return new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          Enum e = (Enum) value;
          out.value(e.name().toLowerCase());
        }

        @Override
        public T read(JsonReader in) throws IOException {
          String name = in.nextString().toUpperCase();

          for (Enum constant : eClass.getEnumConstants()) {
            if (constant.name().toUpperCase().equals(name)) {
              return (T) constant;
            }
          }

          return null;
        }

      };
    }
  }
}