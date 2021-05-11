package net.forthecrown.core.types.interactable;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface InteractionAction extends JsonSerializable, SuggestionProvider<CommandSource> {

    void parse(JsonElement json) throws CommandSyntaxException;
    void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException;

    void onInteract(Player player);

    String getRegistrationName();
    String asString();

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }
}
