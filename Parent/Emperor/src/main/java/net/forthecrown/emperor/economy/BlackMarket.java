package net.forthecrown.emperor.economy;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.emperor.user.CrownUser;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents the Questmoor Black Market
 * <p>Extends CrownSerializer, but does NOT use AbstractSerializer. Extension is just for the methods lol</p>
 */
public interface BlackMarket extends CrownSerializer<CrownCore> {

    /**
     * Gets the current instance of the black market
     * @return The current instance of the black market
     */
    static BlackMarket inst(){
        return CrownCore.getBlackMarket();
    }

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
    Short getItemPrice(Material material);

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
    void setItemPrice(Material material, Short price);

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

    DailyEnchantment getEnchantment();

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

    /**
     * Button used by Edward's inventory to confirm a transaction
     * @return A lime glass pane
     */
    ItemStack getAcceptButton();

    /**
     * Button usd by Edward's inventory to deny a transaction
     * @return
     */
    ItemStack getDenyButton();

    @Override
    default CrownCore getPlugin(){
        return CrownCore.inst();
    }
}
