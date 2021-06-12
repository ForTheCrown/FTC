package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CoreCommands;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.emperor.user.enums.Rank;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class CheckRank implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.inst(), "required_rank");

    private Rank rank;

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        rank = CoreCommands.RANK.parse(reader);
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        parse(null, new StringReader(json.getAsString()));
    }

    @Override
    public Key key() {
        return KEY;
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{rank=" + rank.name().toLowerCase() + "}";
    }

    @Override
    public Component failMessage() {
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

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Rank getRank() {
        return rank;
    }
}
