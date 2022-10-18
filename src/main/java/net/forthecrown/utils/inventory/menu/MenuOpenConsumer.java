package net.forthecrown.utils.inventory.menu;

import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;

/**
 * Menu callback called when a menu is opened.
 * <p>
 * This callback is called before any options are placed
 * into a menu
 */
@FunctionalInterface
public interface MenuOpenConsumer {
    /**
     * Called when the inventory is opened
     * @param user The user opening the inventory
     * @param context The context the inventory is
     *                being opened with
     */
    void onOpen(User user, InventoryContext context);
}