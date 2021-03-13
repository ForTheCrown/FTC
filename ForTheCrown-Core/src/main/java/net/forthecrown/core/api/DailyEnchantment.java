package net.forthecrown.core.api;

import org.bukkit.enchantments.Enchantment;

public interface DailyEnchantment {
    int getBasePrice();

    int getLevel();

    int getPrice();

    Enchantment getEnchantment();

    BlackMarket getOwner();
}
