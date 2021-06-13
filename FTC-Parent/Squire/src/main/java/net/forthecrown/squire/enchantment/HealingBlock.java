package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.server.v1_16_R3.EnchantmentSlotType;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

public class HealingBlock extends RoyalEnchant {

    public HealingBlock() {
        super(
                Squire.createRoyalKey("healingblock"),
                "Healing Block",
                EnchantmentSlotType.WEAPON,
                EnumItemSlot.OFFHAND, EnumItemSlot.MAINHAND
        );
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

}
