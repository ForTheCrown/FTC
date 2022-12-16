package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.inventory.weapon.RoyalSword;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record EndBossUpgrade(int rank) implements WeaponUpgrade {
    public static WeaponUpgrade endBoss(int rank) {
        return new EndBossUpgrade(rank);
    }

    @Override
    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta) {

    }

    @Override
    public Component loreDisplay() {
        return null;
    }
}