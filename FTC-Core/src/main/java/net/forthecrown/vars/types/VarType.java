package net.forthecrown.vars.types;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.ParseFunction;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a type of command variable.
 * <p></p>
 * This tells the variable registry how it could serialize and deserialize the variable, on top of also telling it
 * how to parse it from a string. Types must be able to return a string that could be parsed back into a variable with
 * the {@link VarType#asParsableString(Object)} method
 * <p></p>
 * Several ComVarType constants are stored in the class {@link VarTypes}. Please make sure that only one instance of
 * a command variable exists at any time. Less RAM usage that way
 *
 * @see ComponentVarType
 * @param <T> The type of the var, can be an int, string or any custom type
 */
public interface VarType<T> extends SuggestionProvider<CommandSource>, Keyed, ParseFunction<T>, JsonSerializer<T>, JsonDeserializer<T> {

    /**
     * Creates a parseable string from the given value
     * <p>
     * The result of this method must later also be parseable back into the given value with the
     * {@link VarType#parse(StringReader)} method.
     * </p>
     * @param value The value to turn into a string
     * @return The string representation of the value
     */
    String asParsableString(T value);

    /**
     * Parses The given input into the object
     * @param reader The input to parse from
     * @return The parsed value
     * @throws CommandSyntaxException If the parsing is unsuccessful.
     */
    @Override
    T parse(StringReader reader) throws CommandSyntaxException;

    default Component display(T value){
        if(value == null) return Component.text("null");
        return Component.text(asParsableString(value));
    }

    /**
     * Serializes the object into JSON
     * @param value the value to serialize
     * @return The JSON representation of the given object
     */
    JsonElement serialize(T value);

    /**
     * Deserializes the object from the given JSON input
     * @param element The json to deserialize from
     * @return The deserialized object
     */
    T deserialize(JsonElement element);

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    default @NotNull Var<T> createVar(String name, T value) {
        return new Var<>(this, name, value);
    }

    @Override
    default T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }

    @Override
    default JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src);
    }

    @Override
    @NotNull Key key();
}
