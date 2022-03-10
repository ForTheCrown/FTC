package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class WorldVarType implements VarType<World> {
    private final Key key = Keys.forthecrown("world_type");

    WorldVarType() {
        Registries.VAR_TYPES.register(key, this);
    }

    @Override
    public World parse(StringReader input) throws CommandSyntaxException {
        return WorldArgument.world().parse(input);
    }

    @Override
    public String asParsableString(@Nullable World value) {
        return value == null ? "null" : value.getName();
    }

    @Override
    public JsonElement serialize(@Nullable World value) {
        return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value.getName());
    }

    @Override
    public World deserialize(JsonElement element) {
        if(element == null || element.isJsonNull()) return null;
        return Bukkit.getWorld(element.getAsString());
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> c, SuggestionsBuilder b) {
        return WorldArgument.world().listSuggestions(c, b);
    }
}
