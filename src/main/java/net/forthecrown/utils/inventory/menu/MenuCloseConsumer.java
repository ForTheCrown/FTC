package net.forthecrown.utils.inventory.menu;

import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.user.User;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Callback for menus, called when an inventory
 * is closed
 */
@FunctionalInterface
public interface MenuCloseConsumer {
    /**
     * Called when a menu is being closed
     * @param inventory The inventory being closed
     * @param user The user closing the inventory
     * @param reason The reason it's being closed
     */
    void onClose(FtcInventory inventory, User user, InventoryCloseEvent.Reason reason);
}