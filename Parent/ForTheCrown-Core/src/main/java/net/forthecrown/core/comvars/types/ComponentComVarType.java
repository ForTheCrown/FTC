package net.forthecrown.core.comvars.types;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

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
    public Component fromString(String input) throws CommandSyntaxException {
        try {
            return GsonComponentSerializer.gson().deserialize(input);
        } catch (Exception e){
            throw FtcExceptionProvider.createWithContext("Could not read json", input, 0);
        }
    }

    @Override
    public String asString(Component value) {
        return value == null ? "null" : GsonComponentSerializer.gson().serialize(value);
    }
}