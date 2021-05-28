package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.WorldArgument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public class CheckInWorld implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.getNamespace(), "in_world");

    private World world;

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        world = WorldArgument.world().parse(reader);
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        world = Bukkit.getWorld(json.getAsString());
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{world=" + (world == null ? "null" : world.getName()) + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You cannot use this in this world")
                .color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        if(world == null){
            CrownCore.logger().warning("Null world in checkable");
            return false;
        }

        return player.getWorld().equals(world);
    }

    @Override
    public JsonElement serialize() {
        return world == null ? JsonNull.INSTANCE : new JsonPrimitive(world.getName());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestWorlds(builder);
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
