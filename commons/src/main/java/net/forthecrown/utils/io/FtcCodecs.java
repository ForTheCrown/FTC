package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;
import net.minecraft.core.UUIDUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public @UtilityClass class FtcCodecs {

  /* ----------------------------------------------------------- */

  public final Codec<String> KEY_CODEC = Codec.STRING.comapFlatMap(s -> {
    if (!Registries.isValidKey(s)) {
      return Results.error("Invalid key '%s'", s);
    }

    return DataResult.success(s);
  }, Function.identity());

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
}