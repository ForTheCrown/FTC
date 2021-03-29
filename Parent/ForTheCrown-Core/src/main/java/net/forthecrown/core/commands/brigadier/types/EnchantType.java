package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.v1_16_R3.ArgumentEnchantment;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;

public class EnchantType {
    public static ArgumentEnchantment itemEnchant(){
        return ArgumentEnchantment.a();
    }

    public static Enchantment getEnchantment(CommandContext<CommandListenerWrapper> c, String argument){
        MinecraftKey key = IRegistry.ENCHANTMENT.getKey(ArgumentEnchantment.a(c, argument));
        return new EnchantmentWrapper(key.getKey());
    }
}
