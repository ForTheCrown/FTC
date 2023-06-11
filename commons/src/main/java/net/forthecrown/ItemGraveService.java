package net.forthecrown;

import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An interface which allows FTC plugins to add an filter which is used to test every item in a
 * player's inventory when they die. If any filter returns {@code true} on the item, that item will
 * remain in the player's inventory on death
 *
 */
public interface ItemGraveService {

  /**
   * Gets the grave service instance
   * @return Service instance
   */
  static ItemGraveService grave() {
    return ServiceInstances.getGrave();
  }

  /**
   * Gets an immutable map of all current filters
   * @return Filter map
   */
  Map<String, Filter> getFilters();

  /**
   * Adds a filter with a set ID
   * @param id Filter ID
   * @param filter Item filter
   */
  void addFilter(@NotNull String id, @NotNull Filter filter);

  /**
   * Removes a filter with a specified ID
   * @param id Filter ID
   */
  void removeFilter(@NotNull String id);

  /**
   * Tests if a specified {@code item} should remain in a specified {@code player}'s inventory
   *
   * @param item Item to test
   * @param player Player whose inventory the item is in
   *
   * @return {@code true}, if the item should remain in the player's inventory,
   *         {@code false} otherwise
   */
  boolean shouldRemain(@NotNull ItemStack item, @NotNull Player player);

  /**
   * Single filter instance inside a {@link ItemGraveService}
   */
  @FunctionalInterface
  interface Filter {

    /**
     * Tests if a specified {@code item} should remain in a specified {@code player}'s inventory
     *
     * @param item Item to test
     * @param player Player whose inventory the item is in
     *
     * @return {@code true}, if the item should remain in the player's inventory,
     *         {@code false} otherwise
     */
    boolean shouldRemain(@NotNull ItemStack item, @NotNull Player player);
  }
}