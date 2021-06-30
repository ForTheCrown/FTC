package net.forthecrown.useables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface UsageAction extends JsonSerializable, SuggestionProvider<CommandSource>, Keyed {

    void parse(JsonElement json) throws CommandSyntaxException;
    void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException;

    void onInteract(Player player);

    String asString();

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }
}
