package net.forthecrown.emperor.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.utils.CrownUtils;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

public class KeyComVarType implements ComVarType<Key> {
    public static final ComVarType<Key> KEY = new KeyComVarType();
    private KeyComVarType() {}

    @Override
    public Key fromString(StringReader input) throws CommandSyntaxException {
        return CrownUtils.parseKey(input);
    }

    @Override
    public String asString(@Nullable Key value) {
        return value == null ? "null" : value.asString();
    }

    @Override
    public JsonElement serialize(@Nullable Key value) {
        return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value.asString());
    }
}
