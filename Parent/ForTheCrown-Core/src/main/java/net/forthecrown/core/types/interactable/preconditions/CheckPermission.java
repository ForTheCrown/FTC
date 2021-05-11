package net.forthecrown.core.types.interactable.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.types.interactable.InteractionCheck;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.concurrent.CompletableFuture;

public class CheckPermission implements InteractionCheck {
    private String permission;

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        permission = reader.readUnquotedString();
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        permission = json.getAsString();
    }

    @Override
    public String getRegistrationName() {
        return "required_permission";
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{permission=" + permission + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You don't have permission to use this sign").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return player.hasPermission(permission);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(permission);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(builder, ListUtils.convert(Bukkit.getPluginManager().getPermissions(), Permission::getName));
    }
}
