package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import net.kyori.adventure.text.Component;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public @UtilityClass class FtcCodecs {

  /* ----------------------------------------------------------- */

  public final Codec<String> KEY_CODEC = Codec.STRING.comapFlatMap(s -> {
    if (!Registries.isValidKey(s)) {
      return Results.error("Invalid key '%s'", s);
    }

    return DataResult.success(s);
  }, Function.identity());

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
        try {
          var duration = TomlConfigs.parseDuration(strResult.result().get());
          return DataResult.success(duration);
        } catch (CommandSyntaxException exc) {
          return Results.error(exc.getMessage());
        }
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

  /* ----------------------------------------------------------- */

  public final Codec<Location> LOCATION_CODEC = Codec.of(
      new Encoder<>() {
        @Override
        public <T> DataResult<T> encode(Location input,
                                        DynamicOps<T> ops,
                                        T prefix
        ) {
          var builder = ops.mapBuilder();

          if (input.isWorldLoaded()) {
            builder.add(
                "world",
                ops.createString(input.getWorld().getName())
            );
          }

          builder.add("x", ops.createDouble(input.getX()));
          builder.add("y", ops.createDouble(input.getY()));
          builder.add("z", ops.createDouble(input.getZ()));

          if (input.getYaw() != 0F) {
            builder.add("yaw", ops.createFloat(input.getYaw()));
          }

          if (input.getPitch() != 0F) {
            builder.add("pitch", ops.createFloat(input.getPitch()));
          }

          return builder.build(prefix);
        }
      },

      new Decoder<>() {
        @Override
        public <T> DataResult<Pair<Location, T>> decode(DynamicOps<T> ops,
                                                        T input
        ) {
          Dynamic<T> dynamic = new Dynamic<>(ops, input);
          return dynamic.get("world")
              .asString()
              .flatMap(s -> {
                World w = Bukkit.getWorld(s);

                if (w == null) {
                  return Results.error(
                      "Unknown world: '%s'", s
                  );
                }

                return DataResult.success(w);
              })

              .map(world -> {
                double x = dynamic.get("x")
                    .asNumber()
                    .map(Number::doubleValue)
                    .getOrThrow(false, s -> {
                    });

                double y = dynamic.get("y")
                    .asNumber()
                    .map(Number::doubleValue)
                    .getOrThrow(false, s -> {
                    });

                double z = dynamic.get("z")
                    .asNumber()
                    .map(Number::doubleValue)
                    .getOrThrow(false, s -> {
                    });

                float yaw = dynamic.get("yaw")
                    .asNumber(0F)
                    .floatValue();

                float pitch = dynamic.get("pitch")
                    .asNumber(0F)
                    .floatValue();

                return new Location(world, x, y, z, yaw, pitch);
              })

              .map(location -> Pair.of(location, input));
        }
      }
  );

  public static final Codec<Component> COMPONENT = ofJson(JsonUtils::writeText, JsonUtils::readText);

  public static final Codec<UUID> INT_ARRAY_UUID = UUIDUtil.CODEC;
  public static final Codec<UUID> STRING_UUID = UUIDUtil.STRING_CODEC;

  /* ----------------------------------------------------------- */

  public static <V> Codec<V> ofJson(Function<V, JsonElement> serializer,
                                    Function<JsonElement, V> deserializer
  ) {
    return Codec.of(
        new Encoder<V>() {
          @Override
          public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
            JsonElement json = serializer.apply(input);
            if (ops instanceof JsonOps js) {
              return DataResult.success((T) js);
            }

            T val = JsonOps.INSTANCE.convertTo(ops, json);
            return DataResult.success(val);
          }
        },

        new Decoder<V>() {
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
        map.put(e.name(), e);
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
        if (e.name().equals(s)) {
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