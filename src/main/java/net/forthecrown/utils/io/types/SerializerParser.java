package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A serializer parser is an object which provides a common way
 * to both serialize/deserialize and parse/format a type of object.
 * <p>
 * Originally this class and it's implementations existed as
 * <code>VarType</code>s that were used to serialize, parse and format
 * specific types of global variables. Currently, however, this system
 * is barely used, except for {@link net.forthecrown.waypoint.WaypointProperty}
 * instances.
 * <p>
 * Note that this class uses the DataFixerUpper {@link DynamicOps} interface
 * as a means to serialize/deserialize from any serialization format.
 * @see SerializerParsers
 * @param <T> The type to parse/serialize/deserialize
 */
public interface SerializerParser<T> {

    /**
     * Formats the given value as a string
     * @param value The value to format
     * @return The string representation of the given value
     */
    @NotNull String asString(@NotNull T value);

    /**
     * Formats the given value to text.
     * <p>
     * By default, this will call {@link #asString(Object)}
     * with the given value and pass that onto a text component
     *
     * @param value The value to format
     * @return The text representation of the given value.
     */
    default @NotNull Component display(@NotNull T value) {
        return Component.text(asString(value));
    }

    /**
     * Gets the argument type that parses string input and
     * optionally provides completion suggestions
     * @return This type's argument type
     */
    @NotNull ArgumentType<T> getArgumentType();

    /**
     * Serializes the given value into the given serialization
     * format.
     *
     * @param ops The format to serialize to
     * @param value The value to serialize
     * @return The serialized representation of the given value
     * @param <V> The format's type
     */
    <V> @NotNull V serialize(@NotNull DynamicOps<V> ops, @NotNull T value);

    /**
     * Deserializes a value from the given serialization
     * format's object.
     *
     * @param ops The format to deserialize from
     * @param element The element to deserialize
     * @return The result of the deserialization
     * @param <V> The serialization format's type
     */
    <V> @NotNull DataResult<T> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element);
}