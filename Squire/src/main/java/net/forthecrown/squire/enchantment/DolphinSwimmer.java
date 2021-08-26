package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DolphinSwimmer extends RoyalEnchant {

    public DolphinSwimmer() {
        super(
                Squire.createRoyalKey("dolphinswimmer"),
                "Dolphin Swimmer",
                EnchantmentCategory.TRIDENT,
                EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND
        );
    }

    @Override
    public @NotNull
    EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TRIDENT;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack stack) {
        return stack.getType() == Material.TRIDENT;
    }

    @Override
    public @NotNull Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return new HashSet<>(Arrays.asList(org.bukkit.inventory.EquipmentSlot.HAND, org.bukkit.inventory.EquipmentSlot.OFF_HAND));
    }
}
