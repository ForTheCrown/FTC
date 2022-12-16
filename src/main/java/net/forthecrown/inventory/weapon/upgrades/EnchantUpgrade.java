package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.inventory.weapon.RoyalSword;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record EnchantUpgrade(Enchantment ench, int level) implements WeaponUpgrade {
    @Override
    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta) {
        meta.addEnchant(ench, level, true);
    }

    @Override
    public Component loreDisplay() {
        return ench.displayName(level);
    }
}