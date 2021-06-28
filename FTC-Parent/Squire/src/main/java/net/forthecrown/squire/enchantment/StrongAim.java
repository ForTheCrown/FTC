package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StrongAim extends RoyalEnchant {
    public StrongAim() {
        super(
                Squire.createRoyalKey("strongaim"),
                "Strong Aim",
                EnchantmentCategory.BOW,
                net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.entity.EquipmentSlot.OFFHAND
        );
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return new HashSet<>(Arrays.asList(org.bukkit.inventory.EquipmentSlot.HAND, org.bukkit.inventory.EquipmentSlot.OFF_HAND));
    }
}
