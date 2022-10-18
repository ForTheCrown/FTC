package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.grenadier.types.ByteArgument;
import net.forthecrown.grenadier.types.ShortArgument;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;

import java.util.Map;

/**
 * Class which stores {@link VarType} constants for easy access.
 */
public interface VarTypes {
    Registry<VarType> TYPE_REGISTRY = Registries.newFreezable();
    Map<Class, VarType> BY_TYPE = new Object2ObjectOpenHashMap<>();

    // --- PRIMITIVES ---
    VarType<Byte> BYTE = register("byte_type",
            new SimpleVarType<>(ByteArgument.byteArg(), JsonElement::getAsByte),
            Byte.class, Byte.TYPE
    );

    VarType<Boolean> BOOL = register("boolean_type",
            new SimpleVarType<>(BoolArgumentType.bool(), JsonElement::getAsBoolean),
            Boolean.class, Boolean.TYPE
    );

    VarType<Short> SHORT = register("short_type",
            new SimpleVarType<>(ShortArgument.shortArg(), JsonElement::getAsShort),
            Short.class, Short.TYPE
    );

    VarType<Integer> INT = register("integer_type",
            new SimpleVarType<>(IntegerArgumentType.integer(), JsonElement::getAsInt),
            Integer.class, Integer.TYPE
    );

    VarType<Float> FLOAT = register("float_type",
            new SimpleVarType<>(FloatArgumentType.floatArg(), JsonElement::getAsFloat),
            Float.class, Float.TYPE
    );

    VarType<Double> DOUBLE = register("double_type",
            new SimpleVarType<>(DoubleArgumentType.doubleArg(), JsonElement::getAsDouble),
            Double.class, Double.TYPE
    );

    VarType<Long> LONG = register("long_type",
            new SimpleVarType<>(LongArgumentType.longArg(), JsonElement::getAsLong),
            Long.class, Long.TYPE
    );

    VarType<String> STRING = register("string_type",
            new SimpleVarType<>(StringArgumentType.greedyString(), JsonElement::getAsString),
            String.class
    );

    // --- COMPLEX TYPES ---
    VarType<World> WORLD = register("world_type",
            new WorldVarType(),
            World.class
    );

    VarType<Component> COMPONENT = register("component_type",
            new ComponentVarType(),
            Component.class
    );

    VarType<Long> TIME = register("time_interval",
            new TimeIntervalVarType(),
            Void.class
    );

    static void init() {
        TYPE_REGISTRY.freeze();
    }

    private static <T> VarType<T> register(String key, VarType<T> type, Class... types) {
        for (var t: Validate.notEmpty(types)) {
            BY_TYPE.put(t, type);
        }

        return TYPE_REGISTRY.register(key, type).getValue();
    }
}