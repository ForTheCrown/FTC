package net.forthecrown.squire.enchantment;

import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.EnchantmentSlotType;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class RoyalEnchant extends Enchantment {
    private final String name;
    protected final NMSEnchant handle;

    public RoyalEnchant(@NotNull NamespacedKey key, String name, EnchantmentSlotType type, EnumItemSlot... slots) {
        super(key);
        this.name = name;
        this.handle = new NMSEnchant(name, this, type, slots);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Component displayName(int i) {
        return Component.text(getName());
    }

    public NMSEnchant getHandle(){
        return handle;
    }
}
