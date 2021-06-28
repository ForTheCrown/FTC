package net.forthecrown.squire.enchantment;


import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class RoyalEnchant extends Enchantment {
    private final String name;
    protected final NMSEnchant handle;

    public RoyalEnchant(@NotNull NamespacedKey key, String name, EnchantmentCategory type, EquipmentSlot... slots) {
        super(key);
        this.name = name;
        this.handle = new NMSEnchant(name, this, type, slots);
    }

    public static void addCrownEnchant(ItemStack itemStack, RoyalEnchant enchant, int level){
        itemStack.addUnsafeEnchantment(enchant, level);
        List<Component> lore = new ArrayList<>();
        lore.add(enchant.displayName(level).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if(itemStack.lore() != null) lore.addAll(itemStack.lore());
        itemStack.lore(lore);
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

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public float getDamageIncrease(int i, @NotNull EntityCategory category) {
        return  0f;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }
}
