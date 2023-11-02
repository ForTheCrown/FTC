package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.string.TagParseException;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public @UtilityClass class FtcCodecs {

  /* ----------------------------------------------------------- */

  public final Codec<World> WORLD_CODEC = Codec.STRING.comapFlatMap(s -> {
    NamespacedKey key = NamespacedKey.fromString(s);
    World world;

    if (key != null) {
      world = Bukkit.getWorld(key);
    } else {
      world = Bukkit.getWorld(s);
    }

    if (world == null) {
      return Results.error("Unknown world '%s'", s);
    }

    return Results.success(world);
  }, world -> world.key().asString());

  public final Codec<String> KEY_CODEC = Codec.STRING.comapFlatMap(s -> {
    if (!Registries.isValidKey(s)) {
      return Results.error("Invalid key '%s'", s);
    }

    return DataResult.success(s);
  }, Function.identity());

  public static final Codec<Key> KYORI_KEY = Codec.STRING.comapFlatMap(s -> {
    NamespacedKey key = NamespacedKey.fromString(s);

    if (key == null) {
      return Results.error("Invalid key '%s'", s);
    }

    return Results.success(key);
  }, Key::asString);

  public static final Codec<NamespacedKey> NAMESPACED_KEY = Codec.STRING.comapFlatMap(string -> {
    NamespacedKey key = NamespacedKey.fromString(string);

    if (key == null) {
      return Results.error("Invalid key '%s'", string);
    }

    return Results.success(key);
  }, NamespacedKey::asString);

  public static final Codec<ItemStack> ITEM_CODEC = new PrimitiveCodec<>() {
    @Override
    public <T> DataResult<ItemStack> read(DynamicOps<T> ops, T input) {
      BinaryTag tag;

      if (ops instanceof TagOps) {
        tag = (BinaryTag) input;
      } else {
        tag = ops.convertTo(TagOps.OPS, input);
      }

      if (tag.isString()) {
        try {
          return DataResult.success(ItemStacks.fromNbtString(tag.toString()));
        } catch (TagParseException exc) {
          return Results.error("Invalid Itemstack: " + input);
        }
      }

      if (!tag.isCompound()) {
        return Results.error("Not an object: " + input);
      }

      return DataResult.success(ItemStacks.load(tag.asCompound()));
    }

    @Override
    public <T> T write(DynamicOps<T> ops, ItemStack value) {
      CompoundTag tag = ItemStacks.save(value);

      if (ops instanceof TagOps) {
        return (T) tag;
      } else {
        return TagOps.OPS.convertTo(ops, tag);
      }
    }
  };

  public static final Codec<ItemList> ITEM_LIST_CODEC = ITEM_CODEC.listOf()
      .xmap(ItemLists::newList, itemStacks -> itemStacks);

  public final Codec<Duration> DURATION = new PrimitiveCodec<>() {
    @Override
    public <T> DataResult<Duration> read(DynamicOps<T> ops, T input) {
      var strResult = ops.getStringValue(input);

      if (strResult.result().isPresent()) {
        return safeParse(strResult.result().get(), TomlConfigs::parseDuration);
      }

      return ops.getNumberValue(input)
          .map(number -> Duration.ofMillis(number.longValue()));
    }

    @Override
    public <T> T write(DynamicOps<T> ops, Duration value) {
      return ops.createLong(value.toMillis());
    }
  };

  public final Codec<UUID> UUID_CODEC = new PrimitiveCodec<>() {
    @Override
    public <T> DataResult<UUID> read(DynamicOps<T> ops, T input) {
      var intResult = ops.getIntStream(input)
          .map(intStream -> uuidFromIntArray(intStream.toArray()));

      var stringResult = ops.getStringValue(input)
          .flatMap(s -> {
            try {
              return DataResult.success(UUID.fromString(s));
            } catch (IllegalArgumentException exc) {
              return Results.error("Invalid UUID: '%s'", s);
            }
          });

      if (stringResult.result().isPresent()) {
        return stringResult;
      }

      if (intResult.result().isPresent()) {
        return intResult;
      }

      return Results.error("Not a string or int[]: %s", input);
    }

    @Override
    public <T> T write(DynamicOps<T> ops, UUID value) {
      if (ops instanceof TagOps || ops instanceof NbtOps) {
        return ops.createIntList(Arrays.stream(uuidToIntArray(value)));
      }

      return ops.createString(value.toString());
    }
  };

  public static final Codec<Character> CHAR = Codec.STRING.comapFlatMap(s -> {
    if (s.length() > 1) {
      return Results.error("String '%s' is not a single character!", s);
    }

    if (s.isEmpty()) {
      return Results.error("Empty string");
    }

    return Results.success(s.charAt(0));
  }, Object::toString);

  /* ----------------------------------------------------------- */

  public final Codec<Location> LOCATION_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            WORLD_CODEC.optionalFieldOf("world").forGetter(o -> Optional.ofNullable(o.getWorld())),

            Codec.DOUBLE.fieldOf("x").forGetter(Location::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Location::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Location::getZ),

            Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(Location::getYaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(Location::getPitch)
        )
        .apply(instance, (world, x, y, z, yaw, pitch) -> {
          return new Location(world.orElse(null), x, y, z, yaw, pitch);
        });
  });

  public static final Codec<Component> COMPONENT = ofJson(JsonUtils::writeText, JsonUtils::readText);

  public static final Codec<UUID> INT_ARRAY_UUID = UUIDUtil.CODEC;
  public static final Codec<UUID> STRING_UUID = UUIDUtil.STRING_CODEC;

  /* ----------------------------------------------------------- */

  public static <T extends Keyed> Codec<T> registryCodec(Registry<T> registry) {
    return NAMESPACED_KEY.comapFlatMap(key -> {
      var value = registry.get(key);

      if (value == null) {
        return Results.error("No value named '%s' found", key);
      }

      return Results.success(value);
    }, t -> t.getKey());
  }

  public static <T> DataResult<T> safeParse(String str, ArgumentType<T> parser) {
    try {
      StringReader reader = new StringReader(str);
      return DataResult.success(parser.parse(reader));
    } catch (CommandSyntaxException exc) {
      return DataResult.error(exc::getMessage);
    }
  }

  public static <V> Codec<V> ofJson(
      Function<V, JsonElement> serializer,
      Function<JsonElement, V> deserializer
  ) {
    return Codec.of(
        new Encoder<>() {
          @Override
          public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
            JsonElement json = serializer.apply(input);
            if (ops instanceof JsonOps js) {
              return DataResult.success((T) json);
            }

            T val = JsonOps.INSTANCE.convertTo(ops, json);
            return DataResult.success(val);
          }
        },

        new Decoder<>() {
          @Override
          public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
            JsonElement element;

            if (ops instanceof JsonOps) {
              element = (JsonElement) input;
            } else {
              element = ops.convertTo(JsonOps.INSTANCE, input);
            }

            V value = deserializer.apply(element);
            return DataResult.success(Pair.of(value, input));
          }
        }
    );
  }

  public <E extends Enum<E>> Codec<E> enumCodec(Class<E> eClass) {
    if (!eClass.isEnum()) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is not an enum", eClass)
      );
    }

    E[] constants = eClass.getEnumConstants();

    if (constants.length > 16) {
      Map<String, E> map = new HashMap<>();
      for (var e : constants) {
        map.put(e.name().toUpperCase(), e);
      }

      return Codec.STRING.comapFlatMap(s -> {
        var result = map.get(s.toUpperCase());

        if (result == null) {
          return Results.error(
              "Unknown '%s' constant: '%s'",
              eClass, s
          );
        }

        return DataResult.success(result);
      }, e -> e.name().toLowerCase());
    }

    return Codec.STRING.comapFlatMap(s -> {
      s = s.toUpperCase();

      for (var e : constants) {
        if (e.name().toUpperCase().equals(s)) {
          return DataResult.success(e);
        }
      }

      return Results.error(
          "Unknown '%s' constant: '%s'",
          eClass, s
      );
    }, e -> e.name().toLowerCase());
  }

  public static UUID uuidFromIntArray(int[] arr) {
    return UUIDUtil.uuidFromIntArray(arr);
  }

  public static int[] uuidToIntArray(UUID uuid) {
    return UUIDUtil.uuidToIntArray(uuid);
  }
}