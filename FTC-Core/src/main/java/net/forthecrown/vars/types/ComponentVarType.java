package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for the Component com var type
 */
public class ComponentVarType implements VarType<Component> {
    private final Key key = Keys.forthecrown("component_type");

    ComponentVarType() {
        Registries.VAR_TYPES.register(key, this);
    }

    @Override
    public Component parse(StringReader input) throws CommandSyntaxException {
        return ChatArgument.chat().parse(input);
    }

    @Override
    public String asParsableString(Component value) {
        return value == null ? "null" : GsonComponentSerializer.gson().serialize(value);
    }

    @Override
    public JsonElement serialize(@Nullable Component value) {
        return value == null ? JsonNull.INSTANCE : GsonComponentSerializer.gson().serializeToTree(value);
    }

    @Override
    public Component deserialize(JsonElement element) {
        if(element == null || element.isJsonNull()) return null;
        return GsonComponentSerializer.gson().deserializeFromTree(element);
    }

    @Override
    public Component display(Component value) {
        return value;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}