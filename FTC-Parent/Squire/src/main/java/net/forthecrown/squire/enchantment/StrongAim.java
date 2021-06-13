package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.server.v1_16_R3.EnchantmentSlotType;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

public class StrongAim extends RoyalEnchant {
    public StrongAim() {
        super(
                Squire.createRoyalKey("strongaim"),
                "Strong Aim",
                EnchantmentSlotType.BOW,
                EnumItemSlot.MAINHAND, EnumItemSlot.OFFHAND
        );
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }
}
