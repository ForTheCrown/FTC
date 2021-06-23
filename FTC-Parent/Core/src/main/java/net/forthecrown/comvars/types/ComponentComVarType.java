package net.forthecrown.core.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.Nullable;

/**
 * A class for the Component com var type
 */
public class ComponentComVarType implements ComVarType<Component> {

    /**
     * The instance of the component type
     */
    public static final ComVarType<Component> COMPONENT_TYPE = new ComponentComVarType();

    //Only the class itself may construct this
    private ComponentComVarType() {}

    @Override
    public Component fromString(StringReader input) throws CommandSyntaxException {
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
}