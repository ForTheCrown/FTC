package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.ArgumentType;
import net.forthecrown.grenadier.types.WorldArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class WorldVarType implements VarType<World> {
    @Override
    public @NotNull String asString(@NotNull World value) {
        return value == null ? "null" : value.getName();
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull World value) {
        return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value.getName());
    }

    @Override
    public @NotNull World deserialize(@NotNull JsonElement element) {
        if(element == null || element.isJsonNull()) {
            return null;
        }

        return Bukkit.getWorld(element.getAsString());
    }

    @Override
    public @NotNull ArgumentType<World> getArgumentType() {
        return WorldArgument.world();
    }
}