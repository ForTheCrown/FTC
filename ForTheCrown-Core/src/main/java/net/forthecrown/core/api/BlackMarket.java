package net.forthecrown.core.api;

import net.forthecrown.core.files.FtcUser;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface BlackMarket{
    /**
     * Gets the amount of a material that was sold in the Black Market
     * @param material The material to get the value of
     * @return The amount of Rhines that has been earned from that item, across all players
     */
    Integer getAmountEarned(Material material);

    /**
     * Sets the amount of Rhines earned from a material
     * @param material The material to set the value of
     * @param amount The amount of rhines earned from this material
     */
    void setAmountEarned(Material material, Integer amount);

    /**
     * Checks if a material is sold out
     * @param material The material to check
     * @return if that material is sold out, just checks if the earnings from the item are above 50,000 Rhines or MaxEarnings set in the config
     */
    boolean isSoldOut(Material material);

    /**
     * Gets the price of an item
     * @param material The material to get the price of
     * @return The price of the material
     */
    Integer getItemPrice(Material material);

    /**
     * Checks if a player is allowed to buy an enchant from Edward
     * @param player The player to check
     * @return if the player is allowed to purchase an enchant
     */
    boolean isAllowedToBuyEnchant(Player player);

    /**
     * Sets if a player is allowed to buy an enchant from Edward
     * @param p the player to set
     * @param out
     */
    void setAllowedToBuyEnchant(Player p, boolean out);

    /**
     * Gets if Edward is currently selling enchants
     * @return if Edward is currently selling enchants
     */
    boolean enchantAvailable();

    /**
     * Sets the price of a Black Market Item
     * @param branch Can be one of 3 values: mining, crops, drops
     * @param material The material itself
     * @param price The new price of the material
     */
    void setItemPrice(String branch, Material material, Integer price);

    /**
     * Gets the price of an enchantment
     * @param enchantment The enchantment to get the price of
     * @return The price of that enchantment
     */
    Integer getEnchantPrice(Enchantment enchantment);

    /**
     * Sets the price of an enchantment
     * @param enchantment The enchantment to set
     * @param price The new price
     */
    void setEnchantPrice(Enchantment enchantment, Integer price);

    /**
     * Gets the enchantment being sold currently
     * @return the enchantment being sold today bu Edward
     */
    Enchantment getDailyEnchantment();

    /**
     * Gets NAME's inventory for selling mob drops
     * @param user required user variable
     * @return The seller's inventory
     */
    Inventory getDropInventory(FtcUser user);

    /**
     * Gets NAME's inventory for selling mining related items
     * @param user required user variable
     * @return The seller's inventory
     */
    Inventory getMiningInventory(FtcUser user);

    /**
     * Gets NAME's inventory for selling crops and farming related items
     * @param user required user variable
     * @return The seller's inventory
     */
    Inventory getFarmingInventory(FtcUser user);

    /**
     * Gets Edwards's inventory for selling enchants
     * @return The seller's inventory
     */
    Inventory getEnchantInventory();

    /**
     * Gets Ramun's inventory for selling Parrots
     * @return The seller's inventory
     */
    Inventory getParrotInventory();

    /**
     * Gets the enchantment book being sold currently
     * @return today's enchantment in book form
     */
    ItemStack getDailyEnchantBook();
}
