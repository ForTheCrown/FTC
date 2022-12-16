package net.forthecrown.utils.inventory.menu;

import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A functional interface for {@link MenuNode}
 * for placing items when an inventory is opened
 */
@FunctionalInterface
public interface MenuNodeItem {
    /**
     * Creates an item representing an inventory node
     * @param user The user opening the menu
     * @param context The context the menu is being opened with
     * @return The created item, can be null
     */
    @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context);

    /**
     * Creates a node item which simply
     * returns the given item, without
     * cloning it
     * @param itemStack The item to return
     * @return The created node item
     */
    static MenuNodeItem of(ItemStack itemStack) {
        return (user, context) -> itemStack;
    }

    /**
     * Creates a node which returns the given
     * function's result.
     * <p>
     * The function is essentially a context-less
     * version of the {@link MenuNodeItem} function
     * @param itemProvider The item provider to use
     * @return The created node item
     */
    static MenuNodeItem of(Function<User, ItemStack> itemProvider) {
        return (user, context) -> itemProvider.apply(user);
    }
}