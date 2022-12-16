package net.forthecrown.utils.inventory.menu;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

/**
 * A slot is basically an {@link com.sk89q.worldedit.math.BlockVector2} for
 * inventories.
 * <p>
 * They use a column (X) position and row (Y) position to map themselves to
 * an inventory slot index.
 * <p>
 * Using these coordinates instead of slots, for square container inventories,
 * helps make placing items in them easier and more human-readable, after all,
 * <code>column=2 row=3</code> is a lot easier to understand and visualize than <code>slot=29</code>
 */
@Getter
public class Slot {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * The size of possible column positions (x coordinates) in an inventory,
     * basically max column pos + 1
     */
    public static final int COLUMN_SIZE = 9;

    /**
     * The size of possible row positions (y coordinate) in an inventory,
     * basically max row pos + 1
     */
    public static final int ROW_SIZE = 6;

    /** Slot 0 constant */
    public static final Slot ZERO = new Slot(0, 0);

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** The slot's column (x) position */
    private final byte column;

    /** slot's row (y) position */
    private final byte row;

    /** The slot's inventory index */
    private final int index;

    /* ----------------------------- CONSTRUCTOR ------------------------------ */

    private Slot(int column, int row) {
        // Ensure both column and row are in
        // inventory bounds
        Validate.isTrue(column >= 0 && column < COLUMN_SIZE,
                "Invalid column, must be in range [0..%s], found: %s",
                COLUMN_SIZE - 1, column
        );

        Validate.isTrue(row >= 0 && row < ROW_SIZE,
                "Invalid row, must be in range [0..%s], found: %s",
                ROW_SIZE - 1, row
        );

        this.row = (byte) row;
        this.column = (byte) column;

        this.index = toIndex(column, row);
    }

    /* ----------------------------- STATIC FUNCTIONS ------------------------------ */

    /**
     * Gets the slot value of the given column (x) and row (y)
     * @param column The column (X) position of the slot
     * @param row The row (Y) position of the slot
     * @return The slot at the given coordinates
     */
    public static Slot of(int column, int row) {
        if (column == row && row == 0) {
            return ZERO;
        }

        return new Slot(column, row);
    }

    /**
     * Gets a slot from the given inventory index
     * @param slot The slot to get the object of
     * @return The slot at the given index
     */
    public static Slot of(int slot) {
        if (slot == 0) {
            return ZERO;
        }

        return new Slot(slot % COLUMN_SIZE, slot / COLUMN_SIZE);
    }

    /**
     * Translates the given column and row
     * into a singular inventory index
     * @param column The column (X) position
     * @param row The row (Y) position
     * @return The inventory index
     */
    public static int toIndex(int column, int row) {
        return (row * COLUMN_SIZE) + column;
    }

    /* ----------------------------- INSTANCE METHODS ------------------------------ */

    /**
     * Adds the given amount to this slot's
     * column (X) and row (Y) positions
     * @param column X-axis addition amount
     * @param row Y-axis addition amount
     * @return The slot at that position
     */
    public Slot add(int column, int row) {
        return of(this.column + column, this.row + row);
    }

    // --- OBJECT OVERRIDES ---

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Slot)) {
            return false;
        }

        Slot slot = (Slot) o;

        return index == slot.index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return  "(x=" + column + ", y=" + row + ")";
    }
}