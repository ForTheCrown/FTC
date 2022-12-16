package net.forthecrown.dungeons.enchantments;

import net.forthecrown.core.registry.Keys;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PoisonCrit extends FtcEnchant {
    public PoisonCrit() {
        super(
                Keys.royals("poisoncrit"),
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
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

}