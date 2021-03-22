package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R3.ArgumentItemStack;
import net.minecraft.server.v1_16_R3.ArgumentPredicateItemStack;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.inventory.ItemStack;

public class ItemType {
    public static ArgumentItemStack itemStack(){
        return ArgumentItemStack.a();
    }
    public static ItemStack getItemStack(CommandContext<CommandListenerWrapper> c, String argument, int amount) throws CommandSyntaxException {
        ArgumentPredicateItemStack item = ArgumentItemStack.a(c, argument);
        return item.a(amount, true).asBukkitMirror();
    }

    public static ItemStack getItemStack(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return getItemStack(c, argument, 1);
    }
}
