package net.forthecrown.menu;

import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import org.bukkit.inventory.Inventory;

/**
 * Menu callback called when a menu is opened.
 */
@FunctionalInterface
public interface MenuOpenConsumer {

  /**
   * Called when the inventory is opened
   *
   * @param user      The user opening the inventory
   * @param context   The context the inventory is being opened with
   * @param inventory The inventory that was opened
   */
  void onOpen(User user, Context context, Inventory inventory);
}