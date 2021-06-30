package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.comvars.ParseFunction;
import net.forthecrown.grenadier.CommandSource;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a CommandVariableType, this is needed so a ComVar knows what type a com var actually is
 * <p>Also, this does the string parsing and in the future it might possibly also do the serializing and deserializing</p>
 * <p>Primitive types have the types provided to you, no need to make em</p>
 * <p>I would also recommend making it so you only create one instance of the ComVarType implementation, not several</p>
 * <p>A Component type also exists, I mde it as a test </p>
 * @see ComponentComVarType
 * @param <T> The type of the var, can be an int, string or any custom type
 */
public interface ComVarType<T> extends SuggestionProvider<CommandSource> {
    ComVarType<Long> LONG = new PrimitiveComVarType<>(Long.class, StringReader::readLong, JsonPrimitive::new);
    ComVarType<Double> DOUBLE = new PrimitiveComVarType<>(Double.class, StringReader::readDouble, JsonPrimitive::new);
    ComVarType<Float> FLOAT = new PrimitiveComVarType<>(Float.class, StringReader::readFloat, JsonPrimitive::new);
    ComVarType<Integer> INTEGER = new PrimitiveComVarType<>(Integer.class, StringReader::readInt, JsonPrimitive::new);
    ComVarType<Short> SHORT = new PrimitiveComVarType<>(Short.class, r -> (short) r.readInt(), JsonPrimitive::new);
    ComVarType<Byte> BYTE = new PrimitiveComVarType<>(Byte.class, r -> (byte) r.readInt(), JsonPrimitive::new);
    ComVarType<Boolean> BOOLEAN = new PrimitiveComVarType<>(Boolean.class, StringReader::readBoolean, JsonPrimitive::new);
    ComVarType<String> STRING = new PrimitiveComVarType<>(String.class, StringReader::getRemaining, JsonPrimitive::new);
    ComVarType<Character> CHAR = new PrimitiveComVarType<>(Character.class, StringReader::read, JsonPrimitive::new);

    T fromString(StringReader input) throws CommandSyntaxException;
    String asString(@Nullable T value);

    JsonElement serialize(@Nullable T value);

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> c, SuggestionsBuilder b){
        return Suggestions.empty();
    }

    static CommandSyntaxException mismatchException(String className, String input) {
        return FtcExceptionProvider.createWithContext("Mismatch between input and variable type. Var is: " + className, input, 0);
    }

    class PrimitiveComVarType<T> implements ComVarType<T> {
        private final ParseFunction<T> fromString;
        private final Function<T, JsonElement> serializationFunc;
        private final Class<T> clazz;

        private PrimitiveComVarType(Class<T> clazz, ParseFunction<T> fromString, Function<T, JsonElement> json){
            this.fromString = fromString;
            this.clazz = clazz;
            this.serializationFunc = json;
        }

        @Override
        public T fromString(StringReader input) throws CommandSyntaxException {
            try {
                return fromString.parse(input);
            } catch (RuntimeException e){
                throw mismatchException(clazz.getName(), input.getString());
            }
        }

        @Override
        public String asString(T value) {
            return value == null ? "null" : value.toString();
        }

        @Override
        public JsonElement serialize(@Nullable T value) {
            return value == null ? JsonNull.INSTANCE : serializationFunc.apply(value);
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
    }
}
