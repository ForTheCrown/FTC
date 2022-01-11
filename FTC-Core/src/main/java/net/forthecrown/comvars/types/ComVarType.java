package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.comvars.ParseFunction;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a type of command variable.
 * <p></p>
 * This tells the variable registry how it could serialize and deserialize the variable, on top of also telling it
 * how to parse it from a string. Types must be able to return a string that could be parsed back into a variable with
 * the {@link ComVarType#asParsableString(Object)} method
 * <p></p>
 * Several ComVarType constants are stored in the class {@link ComVarTypes}. Please make sure that only one instance of
 * a command variable exists at any time. Less RAM usage that way
 *
 * @see ComponentComVarType
 * @param <T> The type of the var, can be an int, string or any custom type
 */
public interface ComVarType<T> extends SuggestionProvider<CommandSource>, Keyed, ParseFunction<T> {

    /**
     * Creates a parseable string from the given value
     * <p>
     * The result of this method must later also be parseable back into the given value with the
     * {@link ComVarType#parse(StringReader)} method.
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

    @Override
    @NotNull Key key();
}
