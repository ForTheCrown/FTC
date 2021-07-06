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

public interface UsageType<T> extends SuggestionProvider<CommandSource>, Keyed {
    T parse(StringReader reader, CommandSource source) throws CommandSyntaxException;

    T deserialize(JsonElement element) throws CommandSyntaxException;
    JsonElement serialize(T value);

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    @Override
    @NotNull Key key();
}
