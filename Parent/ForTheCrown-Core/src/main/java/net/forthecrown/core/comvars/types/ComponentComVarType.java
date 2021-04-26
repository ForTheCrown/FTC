package net.forthecrown.core.comvars.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.ComVarException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.v1_16_R3.ArgumentChatComponent;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.concurrent.CompletableFuture;

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
    public Component fromString(String input) throws ComVarException {
        try {
            return GsonComponentSerializer.gson().deserialize(input);
        } catch (Exception e){
            throw new ComVarException("Could not read json", input, input.length());
        }
    }

    @Override
    public String asString(Component value) {
        return value == null ? "null" : GsonComponentSerializer.gson().serialize(value);
    }

    @Override
    public CompletableFuture<Suggestions> suggests(CommandContext<CommandListenerWrapper> c, SuggestionsBuilder b) {
        return ArgumentChatComponent.a().listSuggestions(c, b);
    }
}