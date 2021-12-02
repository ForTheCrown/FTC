package net.forthecrown.inventory.builder;

import net.forthecrown.utils.math.MathUtil;
import org.apache.commons.lang.Validate;

/**
 * Inventory Coordinates, basically a class that holds an X and Y cord value for determining
 * where in the inventory something is or should be.
 */
public class InventoryPos {
    private final byte row;
    private final byte column;

    public InventoryPos(int column, int row) {
        Validate.isTrue(MathUtil.isInRange(column, 0, 8), "Column cannot be more than 8, found: " + column);
        Validate.isTrue(MathUtil.isInRange(row, 0, 5), "Row cannot be more than 5, found: " + row);

        this.row = (byte) row;
        this.column = (byte) column;
    }

    public static InventoryPos fromSlot(int slot) {
        return new InventoryPos(slot % 9, slot / 9);
    }

    public byte getColumn() {
        return column;
    }

    public byte getRow() {
        return row;
    }

    public InventoryPos add(int column, int row) {
        return new InventoryPos(this.column + column, this.row + row);
    }

    /**
     * Gets the absolute inventory index of this cord
     * @return the index of these cords in an inventory
     */
    public int getSlot() {
        return (getRow() * 9) + getColumn();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryPos cords = (InventoryPos) o;

        return cords.getColumn() == this.column && cords.getRow() == this.row;
    }

    @Override
    public int hashCode() {
        return getRow() * getColumn();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + "row=" + getRow() + ", column=" + getColumn() + '}';
    }
}
