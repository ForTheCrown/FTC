package net.forthecrown.utils.inventory.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;

/**
 * Menu callback called when a user clicks in a
 * {@link Menu} menu
 */
@FunctionalInterface
public interface MenuClickConsumer {
    /**
     * Called when a user clicks in an inventory, or
     * when the user clicks on a specific node, determined
     * by which class is implementing this interface.
     * <p>
     * If this class is being used by {@link MenuNode} then
     * this represents that node's click callback, otherwise
     * it will be called for every click in a menu.
     *
     * @param user The user clicking
     * @param context The inventory context
     * @param click The click context, essentially a wrapper for the click event
     *              with some extended features
     * @throws CommandSyntaxException If something went wrong, the exception's failure message
     *                                will be shown to the user in chat
     */
    void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException;

    /**
     * A legacy implementation of {@link MenuClickConsumer} which doesn't
     * accept {@link InventoryContext}
     */
    @FunctionalInterface
    interface Contextless extends MenuClickConsumer {
        /**
         * Called when a user clicks in an inventory
         * @param user The user clicking
         * @param context The click event wrapper
         * @throws CommandSyntaxException If something went wrong, the exception's
         *                                failure message will be shown to the user
         */
        void onClick(User user, ClickContext context) throws CommandSyntaxException;

        @Override
        default void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
            onClick(user, click);
        }
    }
}