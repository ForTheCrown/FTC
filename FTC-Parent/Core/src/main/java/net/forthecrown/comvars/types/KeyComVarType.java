package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.core.CrownCore;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KeyComVarType implements ComVarType<Key> {
    public static final ComVarType<Key> KEY = new KeyComVarType();
    private final Key key = CrownCore.coreKey("key_type");

    private KeyComVarType() {
        Registries.COMVAR_TYPES.register(key, this);
    }

    @Override
    public Key parse(StringReader input) throws CommandSyntaxException {
        return CoreCommands.ftcKeyType().parse(input);
    }

    @Override
    public String asString(@Nullable Key value) {
        return value == null ? "null" : value.asString();
    }

    @Override
    public JsonElement serialize(@Nullable Key value) {
        return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value.asString());
    }

    @Override
    public Key deserialize(JsonElement element) {
        if(element == null || element.isJsonNull()) return null;

        return FtcUtils.parseKey(element.getAsString());
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
