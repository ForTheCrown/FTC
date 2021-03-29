package net.forthecrown.core.api;

import org.bukkit.enchantments.Enchantment;

public interface DailyEnchantment {
    /**
     * Gets the base price of the enchantment
     * @return The base price
     */
    int getBasePrice();

    /**
     * Gets the level of the enchantment
     * @return The enchantment's level
     */
    byte getLevel();

    /**
     * Gets the actual price of the enchantment
     * @return The enchantment's actual price
     */
    int getPrice();

    /**
     * Gets the enchantment itself
     * @return The enchantment
     */
    Enchantment getEnchantment();

    /**
     * Gets the black market
     * @return BM
     */
    BlackMarket getOwner();
}
