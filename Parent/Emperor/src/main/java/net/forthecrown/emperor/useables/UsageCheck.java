package net.forthecrown.emperor.useables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.serialization.JsonSerializable;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface UsageCheck extends JsonSerializable, Predicate<Player>, SuggestionProvider<CommandSource>, Keyed {
    void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException;
    void parse(JsonElement json) throws CommandSyntaxException;

    String asString();

    Component getFailMessage();

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    default Consumer<Player> onSuccess(){
        return null;
    }

    default Component getPersonalizedFailMessage(Player player){
        return getFailMessage();
    }
}