package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.comvars.ParseFunction;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Class which stores ComVarType constants for easy access.
 */
public interface ComVarTypes {
    ComVarType<Long>        LONG =          new PrimitiveComVarType<>(Long.class,           StringReader::readLong,         JsonPrimitive::new,     JsonElement::getAsLong);
    ComVarType<Double>      DOUBLE =        new PrimitiveComVarType<>(Double.class,         StringReader::readDouble,       JsonPrimitive::new,     JsonElement::getAsDouble);
    ComVarType<Float>       FLOAT =         new PrimitiveComVarType<>(Float.class,          StringReader::readFloat,        JsonPrimitive::new,     JsonElement::getAsFloat);
    ComVarType<Integer>     INTEGER =       new PrimitiveComVarType<>(Integer.class,        StringReader::readInt,          JsonPrimitive::new,     JsonElement::getAsInt);
    ComVarType<Short>       SHORT =         new PrimitiveComVarType<>(Short.class,          r -> (short) r.readInt(),       JsonPrimitive::new,     JsonElement::getAsShort);
    ComVarType<Byte>        BYTE =          new PrimitiveComVarType<>(Byte.class,           r -> (byte) r.readInt(),        JsonPrimitive::new,     JsonElement::getAsByte);
    ComVarType<Boolean>     BOOLEAN =       new PrimitiveComVarType<>(Boolean.class,        StringReader::readBoolean,      JsonPrimitive::new,     JsonElement::getAsBoolean);
    ComVarType<String>      STRING =        new PrimitiveComVarType<>(String.class,         StringReader::getRemaining,     JsonPrimitive::new,     JsonElement::getAsString);
    ComVarType<Character>   CHAR =          new PrimitiveComVarType<>(Character.class,      StringReader::read,             JsonPrimitive::new,     JsonElement::getAsCharacter);

    ComVarType<World>       WORLD =         new WorldComVarType();
    ComVarType<Key>         KEY =           new KeyComVarType();
    ComVarType<Component>   COMPONENT =     new ComponentComVarType();

    private static CommandSyntaxException mismatchException(String className, String input) {
        return FtcExceptionProvider.createWithContext("Mismatch between input and variable type. Var is: " + className, input, 0);
    }

    class PrimitiveComVarType<T> implements ComVarType<T> {
        private final ParseFunction<T> fromString;
        private final Function<T, JsonElement> serializationFunc;
        private final Function<JsonElement, T> deserializationFunc;
        private final Class<T> clazz;
        private final Key key;

        private PrimitiveComVarType(Class<T> clazz, ParseFunction<T> fromString, Function<T, JsonElement> json, Function<JsonElement, T> func){
            this.fromString = fromString;
            this.clazz = clazz;
            this.serializationFunc = json;
            deserializationFunc = func;
            key = ForTheCrown.coreKey(clazz.getSimpleName().toLowerCase() + "_type");
            
            Registries.COMVAR_TYPES.register(key, this);
        }

        @Override
        public T parse(StringReader input) throws CommandSyntaxException {
            try {
                return fromString.parse(input);
            } catch (RuntimeException e){
                throw mismatchException(clazz.getName(), input.getString());
            }
        }

        @Override
        public String asParsableString(T value) {
            return value == null ? "null" : value.toString();
        }

        @Override
        public JsonElement serialize(@Nullable T value) {
            return value == null ? JsonNull.INSTANCE : serializationFunc.apply(value);
        }

        @Override
        public T deserialize(JsonElement element) {
            return deserializationFunc.apply(element);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PrimitiveComVarType<?> type = (PrimitiveComVarType<?>) o;
            return clazz.equals(type.clazz);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(clazz)
                    .toHashCode();
        }

        @Override
        public @NotNull Key key() {
            return key;
        }
    }
}
