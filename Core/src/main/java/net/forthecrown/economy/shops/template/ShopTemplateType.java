package net.forthecrown.economy.shops.template;

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

import java.util.concurrent.CompletableFuture;

public interface ShopTemplateType<T extends ShopTemplate> extends Keyed, SuggestionProvider<CommandSource> {

    T parse(Key key, StringReader reader, CommandSource source) throws CommandSyntaxException;

    void edit(T value, StringReader reader, CommandSource source) throws CommandSyntaxException;

    T deserialize(JsonElement element, Key key);

    JsonElement serialize(T value);

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    default CompletableFuture<Suggestions> getEditSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }
}
