package net.forthecrown.dungeons.enchantments;

import io.papermc.paper.enchantments.EnchantmentRarity;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class FtcEnchant extends Enchantment {
    @Getter
    private final EnchantHandle handle;
    @Getter
    private final String name;

    public FtcEnchant(@NotNull NamespacedKey key, String name, EnchantmentCategory type, EquipmentSlot... slots) {
        super(key);

        this.name = name;
        handle = new EnchantHandle(this, type, slots);
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull Component displayName(int i) {
        return Component.text(getName());
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
        return 0f;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public @NotNull String translationKey() {
        return null;
    }

    public void onHurt(LivingEntity user, Entity attacker, int level) {
    }

    public void onAttack(LivingEntity user, Entity target, int level) {
    }
}