package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.types.WorldArgument;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class WorldComVarType implements ComVarType<World> {
    public static final ComVarType<World> WORLD = new WorldComVarType();
    private WorldComVarType() {}

    @Override
    public World fromString(StringReader input) throws CommandSyntaxException {
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
}
