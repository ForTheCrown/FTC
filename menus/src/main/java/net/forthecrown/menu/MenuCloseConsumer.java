package net.forthecrown.menu;

import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Callback for menus, called when an inventory is closed
 */
@FunctionalInterface
public interface MenuCloseConsumer {

  /**
   * Called when a menu is being closed
   *
   * @param inventory The inventory being closed
   * @param user      The user closing the inventory
   * @param reason    The reason it's being closed
   */
  void onClose(Inventory inventory, Context context, User user, InventoryCloseEvent.Reason reason);
}