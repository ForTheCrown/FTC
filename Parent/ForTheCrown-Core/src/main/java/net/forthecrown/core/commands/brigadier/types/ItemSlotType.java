package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.v1_16_R3.ArgumentInventorySlot;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class ItemSlotType {
    public static ArgumentInventorySlot slot(){
        return ArgumentInventorySlot.a();
    }

    public static int getSlot(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentInventorySlot.a(c, argument);
    }
}
