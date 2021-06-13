package net.forthecrown.vikings.valhalla.triggers;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.serializer.JsonSerializable;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.vikings.valhalla.active.ActiveRaid;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.concurrent.CompletableFuture;

public interface TriggerAction<E extends Event> extends JsonSerializable, SuggestionProvider<CommandSource>, Keyed {
    void deserialize(JsonElement element) throws CommandSyntaxException;
    void parse(StringReader reader) throws CommandSyntaxException;

    void trigger(Player player, ActiveRaid raid, E event);

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException{
        return Suggestions.empty();
    }
}
