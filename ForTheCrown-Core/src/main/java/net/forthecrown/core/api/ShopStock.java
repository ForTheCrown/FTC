package net.forthecrown.core.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

public interface ShopStock {

    /**
     * Gets if the stock contains a material
     * @param material The material to check for
     * @return If the stock contains it
     */
    boolean contains(Material material);

    /**
     * Checks if the stock contains a certain amount of a material
     * @param material The material to check for
     * @param amount The minimum amount of it needed
     * @return Whether the stock contains enough of the material
     */
    boolean contains(Material material, @Nonnegative int amount);

    /**
     * Gets if the stock contains the example ItemStack
     * @return Ehhh... check the above line lol
     */
    boolean containsExampleItem();

    /**
     * Removes an amount of items corresponding to the ExampleItem from the inventory
     */
    void removeExampleItemAmount();

    /**
     * Adds an ItemStack to the stock
     * @param stack The ItemStack to add
     */
    void add(@Nonnull ItemStack stack);

    /**
     * Gets if the shop is full or not
     * @return Whether the shop is full or not, aka, if there's more than 27 ItemStacks
     */
    boolean isFull();

    /**
     * Returns whether the stock is empty
     * @return Whether the stock is empty or not
     */
    boolean isEmpty();

    /**
     * Removes a certain amount of a material from the stock
     * @param material The material to remove
     * @param amount The amount of it to remove
     */
    void removeItem(Material material, @Nonnegative int amount);

    /**
     * Gets the contents of the stock
     * @return The contents
     */
    List<ItemStack> getContents();

    /**
     * Sets the contents of the store
     * @param contents The new contents
     */
    void setContents(@Nonnull List<ItemStack> contents);

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
