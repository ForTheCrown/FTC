package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.server.v1_16_R3.EnchantmentSlotType;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

public class PoisonCrit extends RoyalEnchant {

    public PoisonCrit() {
        super(
                Squire.createRoyalKey("poisoncrit"),
                "Critical Poison",
                EnchantmentSlotType.WEAPON,
                EnumItemSlot.OFFHAND, EnumItemSlot.MAINHAND
        );
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

}
