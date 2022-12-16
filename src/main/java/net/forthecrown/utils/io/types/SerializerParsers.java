package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.grenadier.types.ByteArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.ShortArgument;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Class which stores {@link SerializerParser} constants for easy access.
 */
public interface SerializerParsers {
    Registry<SerializerParser> TYPE_REGISTRY = Registries.newFreezable();
    Map<Class, SerializerParser> BY_TYPE = new Object2ObjectOpenHashMap<>();

    /* ----------------------------- PRIMITIVES ------------------------------ */

    SerializerParser<Byte> BYTE = register("byte_type",
            new PrimitiveSerializerParser<>(ByteArgument.byteArg()) {
                @Override
                public <V> @NotNull DataResult<Byte> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getNumberValue(element).map(Number::byteValue);
                }
            },

            Byte.class, Byte.TYPE
    );

    SerializerParser<Boolean> BOOL = register("boolean_type",
            new PrimitiveSerializerParser<>(BoolArgumentType.bool()) {
                @Override
                public <V> @NotNull DataResult<Boolean> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getBooleanValue(element);
                }
            },

            Boolean.class, Boolean.TYPE
    );

    SerializerParser<Short> SHORT = register("short_type",
            new PrimitiveSerializerParser<>(ShortArgument.shortArg()) {
                @Override
                public <V> @NotNull DataResult<Short> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getNumberValue(element).map(Number::shortValue);
                }
            },

            Short.class, Short.TYPE
    );

    SerializerParser<Integer> INT = register("integer_type",
            new PrimitiveSerializerParser<>(IntegerArgumentType.integer()) {
                @Override
                public <V> @NotNull DataResult<Integer> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getNumberValue(element).map(Number::intValue);
                }
            },

            Integer.class, Integer.TYPE
    );

    SerializerParser<Float> FLOAT = register("float_type",
            new PrimitiveSerializerParser<>(FloatArgumentType.floatArg()) {
                @Override
                public <V> @NotNull DataResult<Float> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getNumberValue(element).map(Number::floatValue);
                }
            },
            Float.class, Float.TYPE
    );

    SerializerParser<Double> DOUBLE = register("double_type",
            new PrimitiveSerializerParser<>(DoubleArgumentType.doubleArg()) {
                @Override
                public <V> @NotNull DataResult<Double> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getNumberValue(element).map(Number::doubleValue);
                }
            },
            Double.class, Double.TYPE
    );

    SerializerParser<Long> LONG = register("long_type",
            new PrimitiveSerializerParser<>(LongArgumentType.longArg()) {
                @Override
                public <V> @NotNull DataResult<Long> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getNumberValue(element).map(Number::longValue);
                }
            },
            Long.class, Long.TYPE
    );

    SerializerParser<String> STRING = register("string_type",
            new PrimitiveSerializerParser<>(StringArgumentType.greedyString()) {
                @Override
                public <V> @NotNull DataResult<String> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                    return ops.getStringValue(element);
                }
            },
            String.class
    );

    /* ----------------------------- COMPLEX TYPES ------------------------------ */

    SerializerParser<World> WORLD = register("world_type",
            new WorldSerializerParser(),
            World.class
    );

    SerializerParser<Component> COMPONENT = register("component_type",
            new TextSerializerParser(),
            Component.class
    );

    SerializerParser<Long> TIME = register("time_interval",
            new PeriodSerializerParser(),
            Void.class
    );

    SerializerParser<UUID> UUID = register("uuid_type",
            new UUIDSerializerParser(),
            java.util.UUID.class
    );

    static void init() {
        TYPE_REGISTRY.freeze();
    }

    private static <T> SerializerParser<T> register(String key, SerializerParser<T> type, Class... types) {
        for (var t: Validate.notEmpty(types)) {
            BY_TYPE.put(t, type);
        }

        return TYPE_REGISTRY.register(key, type).getValue();
    }

    static <E extends Enum<E>> SerializerParser<E> ofEnum(Class<E> enumClass) {
        return new SerializerParser<>() {
            @Override
            public @NotNull String asString(@NotNull E value) {
                return value.name().toLowerCase();
            }

            @Override
            public @NotNull ArgumentType<E> getArgumentType() {
                return EnumArgument.of(enumClass);
            }

            @Override
            public <V> @NotNull V serialize(@NotNull DynamicOps<V> ops, @NotNull E value) {
                return ops.createString(value.name().toLowerCase());
            }

            @Override
            public <V> @NotNull DataResult<E> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
                return ops.getStringValue(element)
                        .map(s -> Enum.valueOf(enumClass, s.toUpperCase()));
            }
        };
    }
}