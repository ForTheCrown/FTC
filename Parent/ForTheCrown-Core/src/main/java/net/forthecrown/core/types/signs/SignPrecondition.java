package net.forthecrown.core.types.signs;

import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface SignPrecondition extends JsonSerializable, Predicate<Player>, SuggestionProvider<CommandSource> {
    void parse(String input) throws CommandSyntaxException;
    void parse(JsonElement json) throws CommandSyntaxException;

    String getRegistrationName();
    String asString();

    Component getFailMessage();

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    default Component getPersonalizedFailMessage(Player player){
        return getFailMessage();
    }
}