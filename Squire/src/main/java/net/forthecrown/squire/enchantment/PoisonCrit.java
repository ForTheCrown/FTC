package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PoisonCrit extends RoyalEnchant {

    public PoisonCrit() {
        super(
                Squire.createRoyalKey("poisoncrit"),
                "Critical Poison",
                EnchantmentCategory.WEAPON,
                net.minecraft.world.entity.EquipmentSlot.OFFHAND, net.minecraft.world.entity.EquipmentSlot.MAINHAND
        );
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return new HashSet<>(Arrays.asList(org.bukkit.inventory.EquipmentSlot.HAND, org.bukkit.inventory.EquipmentSlot.OFF_HAND));
    }

}
