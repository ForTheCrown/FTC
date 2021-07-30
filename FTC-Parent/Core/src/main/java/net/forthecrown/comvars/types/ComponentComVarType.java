package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class for the Component com var type
 */
public class ComponentComVarType implements ComVarType<Component> {

    public static final ComVarType<Component> COMPONENT_TYPE = new ComponentComVarType();
    private final Key key = ForTheCrown.coreKey("component_type");

    private ComponentComVarType() {
        Registries.COMVAR_TYPES.register(key, this);
    }

    @Override
    public Component parse(StringReader input) throws CommandSyntaxException {
        try {
            return ComponentArgument.component().parse(input);
        } catch (Exception e){
            throw FtcExceptionProvider.createWithContext("Could not read json", input.getString(), 0);
        }
    }

    @Override
    public String asString(Component value) {
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
    public @NotNull Key key() {
        return key;
    }
}