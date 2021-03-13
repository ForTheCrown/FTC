package net.forthecrown.core.api;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface BlackMarket {

    /**
     * Gets the amount of Rhines earned from a material
     * @param material The material to get
     * @return The amount earned from that material
     */
    Integer getAmountEarned(Material material);

    /**
     * Sets the amount that's been earned from a material
     * @param material The material to set the amount of
     * @param amount The amount that's been earned from it
     */
    void setAmountEarned(Material material, Integer amount);

    /**
     * Checks if a material is sold out, aka if earnings are above the max earning limit
     * @param material The material to check
     * @return Whether it's sold out or not
     */
    boolean isSoldOut(Material material);

    /**
     * Gets the price of an item in BM
     * @param material The material to get the price of
     * @return The price of the material
     */
    Integer getItemPrice(Material material);

    /**
     * Checks if a player is allowed to buy enchantments from Edward
     * @param player The player to check for
     * @return Whether the player is allowed to purchase enchantments
     */
    boolean isAllowedToBuyEnchant(Player player);

    /**
     * Sets if a player is allowed to buy enchants
     * @param p The player to set the value of
     * @param allowed Whether the player is allowed to purchase enchantments
     */
    void setAllowedToBuyEnchant(Player p, boolean allowed);

    /**
     * Checks if Edward is currently selling enchantments
     * @return Whether Edward is selling or not
     */
    boolean enchantAvailable();

    /**
     * Sets the price of an item in the BM
     * @param material The material to set the price of
     * @param price The new price of the item
     */
    void setItemPrice(Material material, Integer price);

    /**
     * Gets the price of an enchantment
     * @param enchantment The enchantment to get the price of
     * @return The price
     */
    Integer getEnchantBasePrice(Enchantment enchantment);

    /**
     * Sets the price of an enchantment
     * @param enchantment The enchantment to set the price of
     * @param price The new price
     */
    void setEnchantBasePrice(Enchantment enchantment, Integer price);

    DailyEnchantment getDailyEnchantment();

    /**
     * Gets the inventory of Otto, aka the drops inventory
     * @param user The user opening the inventory
     * @return The inventory
     */
    Inventory getDropInventory(CrownUser user);

    Inventory getMiningInventory(CrownUser user);

    Inventory getFarmingInventory(CrownUser user);


    Inventory getEnchantInventory(ItemStack userItem, boolean accepting);

    Inventory getParrotInventory();

    ItemStack getAcceptEnchantButton();

    ItemStack getDenyEnchantButton();

    void save();

    void reload();
}
