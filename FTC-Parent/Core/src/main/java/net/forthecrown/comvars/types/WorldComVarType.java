package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldComVarType implements ComVarType<World> {
    public static final ComVarType<World> WORLD = new WorldComVarType();
    private final Key key = CrownCore.coreKey("world_type");

    private WorldComVarType() {
        Registries.COMVAR_TYPES.register(key, this);
    }

    @Override
    public World parse(StringReader input) throws CommandSyntaxException {
        return WorldArgument.world().parse(input);
    }

    @Override
    public String asString(@Nullable World value) {
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
}
