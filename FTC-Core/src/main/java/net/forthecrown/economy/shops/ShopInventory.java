package net.forthecrown.economy.shops;

import net.forthecrown.inventory.FtcInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Represents the inventory of a sign shop
 * <p></p>
 * Implementation: {@link FtcShopInventory}
 */
public interface ShopInventory extends FtcInventory {
    /**
     * Gets if the shop is full or not
     * @return Whether the shop is full or not, aka, if there's more than 27 ItemStacks
     */
    boolean isFull();

    /**
     * Gets the stock's example item
     * <p>This thing can go absolutely fuck itself. I swear to god there is no part of this code worse than this little thing called an exampleItem
     * I absolutely hate it.</p>
     * <p>The exampleItem is used as the base of all shop interactions, obviously.</p>
     * @return The stock's example item
     */
    ItemStack getExampleItem();

    /**
     * Sets the exampleItem
     * @param exampleItem the new ExampleItem
     */
    void setExampleItem(ItemStack exampleItem);

    /**
     * Sets the shops contents and sorts it
     * @param list The new contents of the shop
     */
    void setShopContents(Collection<ItemStack> list);

    /**
     * Gets the shops contents without without any null item stacks
     * @return A list of item stacks in the shop
     */
    List<ItemStack> getShopContents();

    /**
     * Clears the stock's contents
     * <p>Doesn't reset the example item</p>
     */
    void clear();

    /**
     * Gets the SignShop belonging to the open inventory
     *
     * @return The holder of the inventory
     */
    @Override
    @NotNull SignShop getHolder();

    /**
     * Gets the SignShop belonging to the open inventory
     *
     * @param useSnapshot Create a snapshot if the holder is a tile entity
     * @return The holder of the inventory
     */
    @Override
    @NotNull SignShop getHolder(boolean useSnapshot);

    /**
     * Checks if the shop has enough stock
     * And changes the ingame sign accordingly
     * @return True, if the inventory is 'in stock', false otherwise
     */
    boolean inStock();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
