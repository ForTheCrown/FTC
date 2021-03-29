package net.forthecrown.core.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Grave {
    /**
     * Gets the user that the grave belongs to
     * @return The grave's owner
     */
    CrownUser getUser();

    /**
     * Attempts to give the grave's items back to the owner
     */
    void giveItems();

    /**
     * Gets whether the grave is empty
     * @return ^^^^
     */
    boolean isEmpty();

    /**
     * Adds an item into the grave
     * @param item The item to add
     */
    void addItem(@NotNull ItemStack item);

    /**
     * Adds items into the grave
     * @param items The items to add
     */
    void addItem(@NotNull ItemStack... items);

    /**
     * Sets the contents of the grave
     * @param items The contents
     */
    void setItems(@NotNull List<ItemStack> items);

    /**
     * Gets all items currently in the grave
     * @return Grave items
     */
    List<ItemStack> getItems();
}
