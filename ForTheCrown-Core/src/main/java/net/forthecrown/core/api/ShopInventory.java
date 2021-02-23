package net.forthecrown.core.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public interface ShopInventory extends Inventory {
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
     * Stores the given ItemStacks in the inventory. This will try to fill
     * existing stacks and empty slots as well as it can.
     * <p>
     * The returned HashMap contains what it couldn't store, where the key is
     * the index of the parameter, and the value is the ItemStack at that
     * index of the varargs parameter. If all items are stored, it will return
     * an empty HashMap.
     * <p>
     * If you pass in ItemStacks which exceed the maximum stack size for the
     * Material, first they will be added to partial stacks where
     * Material.getMaxStackSize() is not exceeded, up to
     * Material.getMaxStackSize(). When there are no partial stacks left
     * stacks will be split on Inventory.getMaxStackSize() allowing you to
     * exceed the maximum stack size for that material.
     * <p>
     * It is known that in some implementations this method will also set
     * the inputted argument amount to the number of that item not placed in
     * slots.
     *
     * @param items The ItemStacks to add
     * @return A HashMap containing items that didn't fit.
     * @throws IllegalArgumentException if items or any element in it is null
     */
    @Override
    HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException;

    /**
     * Sets the exampleItem
     * @param exampleItem the new ExampleItem
     */
    void setExampleItem(ItemStack exampleItem);

    /**
     * Sets the exampleItem and adds it to the stock's contents
     * @param exampleItem The new ExampleItem
     */
    void setExampleItemAndAdd(ItemStack exampleItem);

    /**
     * Gets the shop that owns this stock
     * @return The shop that owns this stock
     */
    SignShop getOwningShop();

    /**
     * Sets the shops contents and sorts it
     * @param list The new contents of the shop
     */
    void setShopContents(List<ItemStack> list);

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

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
