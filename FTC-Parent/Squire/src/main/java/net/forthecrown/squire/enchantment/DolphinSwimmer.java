package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.server.v1_16_R3.EnchantmentSlotType;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DolphinSwimmer extends RoyalEnchant {

    public DolphinSwimmer() {
        super(
                Squire.createRoyalKey("dolphinswimmer"),
                "Dolphin Swimmer",
                EnchantmentSlotType.TRIDENT,
                EnumItemSlot.OFFHAND, EnumItemSlot.MAINHAND
        );
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TRIDENT;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack stack) {
        return stack.getType() == Material.TRIDENT;
    }
}
