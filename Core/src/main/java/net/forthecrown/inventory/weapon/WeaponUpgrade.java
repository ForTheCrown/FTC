package net.forthecrown.inventory.weapon;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * An upgrade is something that is applied to a weapon when it ascends
 * to a certain rank
 */
public interface WeaponUpgrade {

    /**
     * Applies the upgrade to the given item
     * @param item The ItemStack itself
     * @param meta The item's meta
     * @param nbt The item's RoyalItem tag
     */
    void apply(ItemStack item, ItemMeta meta, CompoundTag nbt);
    Component loreDisplay();

    /**
     * Creates an upgrade that applies an enchantment to the item
     * @param enchantment The enchantment to apply
     * @param level The level of the enchantment
     * @return The created upgrade
     */
    static WeaponUpgrade enchantment(Enchantment enchantment, int level) {
        return new WeaponUpgrade() {
            @Override
            public void apply(ItemStack item, ItemMeta meta, CompoundTag nbt) {
                meta.addEnchant(enchantment, level, true);
            }

            @Override
            public Component loreDisplay() {
                return enchantment.displayName(level);
            }
        };
    }
}
