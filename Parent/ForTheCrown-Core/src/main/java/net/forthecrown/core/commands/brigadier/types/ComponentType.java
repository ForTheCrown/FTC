package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.ArgumentChatComponent;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;

public class ComponentType {

    public static ArgumentChatComponent component(){
        return ArgumentChatComponent.a();
    }

    public static IChatBaseComponent getVanilla(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentChatComponent.a(c, argument);
    }

    public static Component getAdventure(CommandContext<CommandListenerWrapper> c, String argument){
        return PaperAdventure.asAdventure(getVanilla(c, argument));
    }
}
