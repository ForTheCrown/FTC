package net.forthecrown.utils.inventory.menu;

import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.utils.inventory.menu.Slot.COLUMN_SIZE;
import static net.forthecrown.utils.inventory.menu.Slot.ROW_SIZE;

/**
 * Utility class for menus
 * @see Menu
 * @see MenuBuilder
 * @see MenuNode
 * @see MenuNodeItem
 */
public final class Menus {
    private Menus() {}

    /* ----------------------------- CONSTANTS ------------------------------ */

    /** Default size assigned to all inventories */
    public static final int DEFAULT_INV_SIZE = 27;

    /** Minimum size of an inventory */
    public static final int MIN_INV_SIZE = 9;

    /** Maximum size of an inventory */
    public static final int MAX_INV_SIZE = COLUMN_SIZE * ROW_SIZE;

    /** Default border item, gray stained-glass pane */
    private static final ItemStack DEFAULT_BORDER = createBorderItem(Material.GRAY_STAINED_GLASS_PANE);

    /* ----------------------------- FUNCTIONS ------------------------------ */

    /** Creates a clone of {@link #DEFAULT_BORDER} */
    public static ItemStack defaultBorderItem() {
        return DEFAULT_BORDER.clone();
    }

    /**
     * Creates a border item with the given
     * material and empty name
     * @param material The material to use for the item
     * @return The created item
     */
    public static ItemStack createBorderItem(Material material) {
        return ItemStacks.builder(material)
                .setName(" ")
                .build();
    }

    /**
     * Gets an inventory size from the given amount
     * of rows
     * @param rows The amount of rows the size should have
     * @return The size
     */
    public static int sizeFromRows(int rows) {
        return rows * COLUMN_SIZE;
    }

    /**
     * Tests if the given size is valid for an inventory.
     * <p>
     * Bukkit states that a valid size 'is a multiple of 9'
     * but that's not the whole truth, the size cannot be less
     * than {@link #MIN_INV_SIZE} and cannot be greater than
     * {@link #MAX_INV_SIZE} and the size must be a multiple of
     * 9 as well.
     *
     * @param size The size to test
     * @return True, if the size is a multiple of 9, bigger than
     *         8 and less than 55
     */
    public static boolean isValidSize(int size) {
        return size >= MIN_INV_SIZE && size <= MAX_INV_SIZE && size % COLUMN_SIZE == 0;
    }

    /**
     * Validates the given inventory size
     * @param size The size to validate
     * @return The given size
     * @throws IllegalArgumentException If the size was invalid
     * @see #isValidSize(int) for specification on what is a valid size
     */
    public static int validateSize(int size) throws IllegalArgumentException {
        Validate.isTrue(
                isValidSize(size),

                "Invalid menu size: %s, must be divisible by 9 and in bounds [9..54]",
                size
        );

        return size;
    }

    public static void placeBorder(Inventory in, ItemStack item) {
        var finalRow = in.getSize() - COLUMN_SIZE - 1;

        for (int i = 0; i < in.getSize(); i++) {
            // If we're on the first or last row
            // then just place item
            if (i < COLUMN_SIZE || i > finalRow) {
                setIfEmpty(in, i, item);
            } else if (i % COLUMN_SIZE == 0) {
                // We're on the left side of the inventory
                // Place item and move to the right, place
                // and then continue loop
                setIfEmpty(in, i, item);
                setIfEmpty(in, i += (COLUMN_SIZE - 1), item);
            }
        }
    }

    private static void setIfEmpty(Inventory i, int slot, ItemStack item) {
        var existing = i.getItem(slot);

        if (ItemStacks.notEmpty(existing)) {
            return;
        }

        i.setItem(slot, item);
    }

    public static boolean isBorderSlot(Slot slot, int size) {
        return slot.getRow() == 0
                || slot.getRow() == (size / COLUMN_SIZE)
                || slot.getColumn() == 0
                || slot.getColumn() == (COLUMN_SIZE - 1);
    }

    /**
     * Creates a node that opens the given menu and is
     * displayed using the given item
     * @param menu The menu the node opens
     * @param item The item the node uses
     * @return The created node
     */
    public static MenuNode createOpenNode(Menu menu, MenuNodeItem item) {
        return MenuNode.builder()
                .setItem(item)
                .setRunnable((user, context, click) -> menu.open(user, context))
                .build();
    }

    /**
     * Creates a menu builder with {@link #DEFAULT_INV_SIZE}
     * for the size
     * @return The created builder
     * @see MenuBuilder
     */
    public static MenuBuilder builder() {
        return new MenuBuilder();
    }

    /**
     * Creates a menu builder with the given
     * size, {@link #isValidSize(int)} must return
     * true for the given size else an exception
     * will be thrown
     * @param size The size of the menu
     * @return The created builder
     * @throws IllegalArgumentException If the given size is invalid
     * @see #builder()
     * @see MenuBuilder#setSize(int)
     */
    public static MenuBuilder builder(int size) throws IllegalArgumentException {
        return builder().setSize(size);
    }

    /**
     * Creates a menu with the given title
     * @param title The title of the menu
     * @return The created builder
     * @see #builder()
     * @see MenuBuilder#setTitle(Component)
     */
    public static MenuBuilder builder(@Nullable Component title) {
        return builder().setTitle(title);
    }

    /**
     * Creates a menu builder with the given size and
     * title. {@link #isValidSize(int)} must return true
     * for the given size else an exception will be thrown
     * @param size The size of the inventory
     * @param title The inventory's title
     * @return The created builder
     * @throws IllegalArgumentException If the size is invalid
     * @see #builder(int)
     * @see MenuBuilder#setTitle(Component)
     */
    public static MenuBuilder builder(int size, @Nullable Component title) throws IllegalArgumentException {
        return builder(size).setTitle(title);
    }

    /**
     * Creates a menu builder with the given size and
     * title. {@link #isValidSize(int)} must return true
     * for the given size else an exception will be thrown
     * @param size The size of the inventory
     * @param title The inventory's title
     * @return The created builder
     * @throws IllegalArgumentException If the size is invalid
     * @see #builder(int)
     * @see MenuBuilder#setTitle(String)
     */
    public static MenuBuilder builder(int size, @NotNull String title) throws IllegalArgumentException {
        return builder(size).setTitle(title);
    }
}