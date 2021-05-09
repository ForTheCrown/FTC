package net.forthecrown.core.types.signs.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.CoreCommands;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.types.signs.SignPrecondition;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SignCheckRank implements SignPrecondition {
    private Rank rank;

    @Override
    public void parse(String input) throws CommandSyntaxException {
        rank = CoreCommands.RANK.parse(new StringReader(input));
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        parse(json.getAsString());
    }

    @Override
    public String getRegistrationName() {
        return "required_rank";
    }

    @Override
    public String asString() {
        return  "SignCheckRank{rank=" + rank.name().toLowerCase() + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You need the ")
                .color(NamedTextColor.GRAY)
                .append(rank.prefix())
                .append(Component.text("rank."));
    }

    @Override
    public boolean test(Player player) {
        return UserManager.getUser(player).hasRank(rank);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(rank.name().toLowerCase());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CoreCommands.RANK.listSuggestions(context, builder);
    }
}
