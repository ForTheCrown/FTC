package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.WorldArgument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CheckInWorld implements UsageCheck<CheckInWorld.CheckInstance> {
    public static final Key KEY = Key.key(Crown.inst(), "in_world");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        World world = WorldArgument.world().parse(reader);
        return new CheckInstance(world);
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(Bukkit.getWorld(element.getAsString()));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return new JsonPrimitive(value.getWorld().getName());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestWorlds(builder);
    }

    public static class CheckInstance implements UsageCheckInstance {
        private final World world;

        public CheckInstance(World world) {
            this.world = world;
        }

        public World getWorld() {
            return world;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "world=" + world.getName() + '}';
        }

        @Override
        public Component failMessage() {
            return Component.text("You cannot use this in this world")
                    .color(NamedTextColor.GRAY);
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            return player.getWorld().equals(world);
        }
    }
}
