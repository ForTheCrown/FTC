package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.v1_16_R3.ArgumentItemStack;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemType {
    public static ArgumentItemStack itemStack(){
        return ArgumentItemStack.a();
    }
    public static ItemStack getItemStack(CommandContext<CommandListenerWrapper> c, String argument){
        return CraftItemStack.asBukkitCopy(ArgumentItemStack.a(c, argument).a().createItemStack());
    }
}
