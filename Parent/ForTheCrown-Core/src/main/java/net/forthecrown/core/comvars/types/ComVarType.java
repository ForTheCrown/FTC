package net.forthecrown.core.comvars.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.ComVarException;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
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
public interface ComVarType<T> {
    ComVarType<Long> LONG = new PrimitiveComVarType<>(Long.class, Long::parseLong);
    ComVarType<Double> DOUBLE = new PrimitiveComVarType<>(Double.class, Double::parseDouble);
    ComVarType<Float> FLOAT = new PrimitiveComVarType<>(Float.class, Float::parseFloat);
    ComVarType<Integer> INTEGER = new PrimitiveComVarType<>(Integer.class, Integer::parseInt);
    ComVarType<Short> SHORT = new PrimitiveComVarType<>(Short.class, Short::parseShort);
    ComVarType<Byte> BYTE = new PrimitiveComVarType<>(Byte.class, Byte::parseByte);
    ComVarType<Boolean> BOOLEAN = new PrimitiveComVarType<>(Boolean.class, Boolean::parseBoolean);
    ComVarType<String> STRING = new PrimitiveComVarType<>(String.class, String::toString);
    ComVarType<Character> CHAR = new PrimitiveComVarType<>(Character.class, str -> str.charAt(0));

    T fromString(String input) throws ComVarException;
    String asString(@Nullable T value);
    default CompletableFuture<Suggestions> suggests(CommandContext<CommandListenerWrapper> c, SuggestionsBuilder b){
        return Suggestions.empty();
    }

    static ComVarException mismatchException(String className, String input) {
        return new ComVarException("Mismatch between input and variable type. Var is: " + className, input, input.length());
    }

    class PrimitiveComVarType<T> implements ComVarType<T> {
        private final Function<String, T> fromString;
        private final Class<T> clazz;
        public PrimitiveComVarType(Class<T> clazz, Function<String, T> fromString){
            this.fromString = fromString;
            this.clazz = clazz;
        }

        @Override
        public T fromString(String input) throws ComVarException {
            try {
                return fromString.apply(input);
            } catch (RuntimeException e){
                throw mismatchException(clazz.getName(), input);
            }
        }

        @Override
        public String asString(T value) {
            return value == null ? "null" : value.toString();
        }
    }
}
