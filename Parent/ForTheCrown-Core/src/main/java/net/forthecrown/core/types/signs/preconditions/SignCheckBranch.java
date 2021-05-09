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
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.types.signs.SignPrecondition;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SignCheckBranch implements SignPrecondition {
    private Branch branch;

    @Override
    public void parse(String input) throws CommandSyntaxException {
        branch = CoreCommands.BRANCH.parse(new StringReader(input));
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        parse(json.getAsString());
    }

    @Override
    public String getRegistrationName() {
        return "required_branch";
    }

    @Override
    public String asString() {
        return "SignCheckBranch{branch=" + branch.getName() + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You need to be a ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(branch.getSingularName()).color(NamedTextColor.GOLD))
                .append(Component.text(" to use this sign."));
    }

    @Override
    public boolean test(Player player) {
        return UserManager.getUser(player).getBranch() == branch;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(branch.name().toLowerCase());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CoreCommands.BRANCH.listSuggestions(context, builder);
    }
}
