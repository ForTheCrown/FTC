package net.forthecrown.useables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a usage type
 * @param <T> The type
 */
public interface UsageType<T extends UsageTypeInstance> extends SuggestionProvider<CommandSource>, Keyed {

    /**
     * Parses the given input into the type
     * @param reader The input
     * @param source The source of the input
     * @return The parse result of the input
     * @throws CommandSyntaxException If something goes wrong lol, idk
     */
    T parse(StringReader reader, CommandSource source) throws CommandSyntaxException;

    /**
     * Deserializes the type from the given {@link JsonElement}.
     * @param element The element to deserialize from
     * @return The deserialized result of the input
     * @throws CommandSyntaxException If something goes wrong idk
     */
    T deserialize(JsonElement element) throws CommandSyntaxException;

    /**
     * Serializes the given type to JSON
     * @param value The type to serialize
     * @return The serialized version of the given value
     */
    JsonElement serialize(T value);

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    /**
     * Checks whether the type requires parsing input
     * @return True if parse input is required by this type, false if not
     */
    default boolean requiresInput() {
        return true;
    }

    /**
     * Gets the key of this implementation of the type.
     * @return The key
     */
    @Override
    @NotNull Key key();
}
