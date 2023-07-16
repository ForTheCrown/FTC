package net.forthecrown;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface InventoryStorage {

  /**
   * Gets the Inventory storage service
   * @return Inventory storage service
   * @throws IllegalStateException If the service implementation couldn't be found
   */
  static InventoryStorage getInstance() throws IllegalStateException {
    return ServiceInstances.getInventoryStorage();
  }

  /**
   * Clears all stored inventories
   */
  void clear();

  /**
   * Swaps the player's inventory with the inventory stored in the given
   * category.
   * <p>
   * If the player has no stored inventory, their items are stored and inventory
   * cleared. If they do have a stored inventory, then the stored items are
   * returned to the player after their current inventory is stored.
   *
   * @param player   The player whose inventory to swap
   * @param category The category to swap items in
   */
  void swap(@NotNull Player player, @NotNull String category);

  /**
   * Stores a player's current inventory.
   * <p>
   * Note: This does NOT clear the player's inventory afterward
   *
   * @param player     The player whose inventory to save
   * @param category   The category to save the player's current inventory into
   * @throws IllegalArgumentException If there is already an inventory saved in
   *                                  given category for this player
   */
  void storeInventory(@NotNull Player player, @NotNull String category);

  /**
   * Tests if the player has a stored inventory in the given category
   *
   * @param player   The player to test
   * @param category The name of the category
   * @return True, if the player has a stored inventory in the given category,
   * false otherwise
   */
  boolean hasStoredInventory(@NotNull Player player, @NotNull String category);

  /**
   * Returns all the contents of a player's stored inventory to the player
   *
   * @param player       The player to return the items to
   * @param category     The name of the category to get the items from
   *
   * @return True, if the player had an inventory saved in the given category,
   * false otherwise
   */
  boolean returnItems(@NotNull Player player, @NotNull String category);

  /**
   * Gives the items to the player without removing them from the storage.
   * <p>
   * Different from {@link #returnItems(Player, String)} because it
   * doesn't remove the items from storage before returning them to the user.
   *
   * @param player   The player to return the items to
   * @param category The category to get the items from
   *
   * @return True, if the player had items to give, false if the player had no
   *         storage entry, or if the player had no items saved in the category
   */
  boolean giveItems(@NotNull Player player, @NotNull String category);

  Int2ObjectMap<ItemStack> removeItems(@NotNull Player player, @NotNull String category);

  Set<String> getExistingCategories(@NotNull Player player);
}