package net.forthecrown.utils.inventory;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.commons.lang3.Validate;

/**
 * A slot is basically an 2D integer vector for inventory slot positions.
 * <p>
 * They use a column (X) position and row (Y) position to map themselves to an inventory slot
 * index.
 * <p>
 * Using these coordinates instead of slots, for square container inventories, helps make placing
 * items in them easier and more human-readable, after all,
 * <code>column=2 row=3</code> is a lot easier to understand and visualize than
 * <code>slot=29</code>
 */
@Getter
public class Slot {

  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * The size of possible column positions (x coordinates) in an inventory, basically max column pos
   * + 1
   */
  public static final int COLUMN_SIZE = 9;

  /**
   * The size of possible row positions (y coordinate) in an inventory, basically max row pos + 1
   */
  public static final int ROW_SIZE = 6;

  public static final Slot ZERO;

  private static final Slot[]   INDEX_CACHE;
  private static final Slot[][] XY_CACHE;

  static {
    int slotsSize = COLUMN_SIZE * ROW_SIZE;
    INDEX_CACHE = new Slot[slotsSize];
    XY_CACHE = new Slot[COLUMN_SIZE][ROW_SIZE];

    // Cache all slots
    int i = 0;
    for (int col = 0; col < COLUMN_SIZE; col++) {
      for (int row = 0; row < ROW_SIZE; row++) {
        Slot slot = new Slot(col, row, i);

        INDEX_CACHE[i] = slot;
        XY_CACHE[col][row] = slot;

        i++;
      }
    }

    ZERO = INDEX_CACHE[0];
  }

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * The slot's column (x) position
   */
  private final byte column;

  /**
   * slot's row (y) position
   */
  private final byte row;

  /**
   * The slot's inventory index
   */
  private final int index;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  private Slot(int column, int row, int index) {
    this.row = (byte) row;
    this.column = (byte) column;
    this.index = index;
  }

  /* -------------------------- STATIC FUNCTIONS -------------------------- */

  /**
   * Gets the slot value of the given column (x) and row (y)
   *
   * @param column The column (X) position of the slot
   * @param row    The row (Y) position of the slot
   * @return The slot at the given coordinates
   */
  public static Slot of(int column, int row) {
    validateSlot(column, row);
    return XY_CACHE[column][row];
  }

  /**
   * Gets a slot from the given inventory index
   *
   * @param slot The slot to get the object of
   * @return The slot at the given index
   */
  public static Slot of(int slot) {
    return INDEX_CACHE[slot];
  }

  /**
   * Translates the given column and row into a singular inventory index
   *
   * @param column The column (X) position
   * @param row    The row (Y) position
   * @return The inventory index
   */
  public static int toIndex(int column, int row) {
    return (row * COLUMN_SIZE) + column;
  }

  public static void validateSlot(int index) {
    Objects.checkIndex(index, INDEX_CACHE.length);
  }

  public static void validateSlot(int col, int row) {
    // Ensure both column and row are in
    // inventory bounds
    Validate.isTrue(col >= 0 && col < COLUMN_SIZE,
        "Invalid column, must be in range [0..%s], found: %s",
        COLUMN_SIZE - 1, col
    );

    Validate.isTrue(row >= 0 && row < ROW_SIZE,
        "Invalid row, must be in range [0..%s], found: %s",
        ROW_SIZE - 1, row
    );
  }

  /* ------------------------- INSTANCE METHODS --------------------------- */

  /**
   * Adds the given amount to this slot's column (X) and row (Y) positions
   *
   * @param column X-axis addition amount
   * @param row    Y-axis addition amount
   * @return The slot at that position
   */
  public Slot add(int column, int row) {
    return of(this.column + column, this.row + row);
  }

  /* ----------------------------- LOADER ------------------------------ */

  public static Slot load(JsonElement element) {
    if (element.isJsonPrimitive()) {
      int number = element.getAsInt();
      return of(number);
    }

    if (element.isJsonArray()) {
      JsonArray arr = element.getAsJsonArray();

      Preconditions.checkState(arr.size() == 2,
          "Slot array must be made of 2 numbers, a column and row position"
      );

      int col = arr.get(0).getAsInt();
      int row = arr.get(1).getAsInt();

      return of(col, row);
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    Preconditions.checkState(json.has("row"), "No 'row' element in JSON");
    Preconditions.checkState(json.has("column"), "No 'column' element in JSON");

    int col = json.getInt("column");
    int row = json.getInt("row");

    return of(col, row);
  }

  // --- OBJECT OVERRIDES ---

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Slot slot)) {
      return false;
    }

    return index == slot.index;
  }

  @Override
  public int hashCode() {
    return index;
  }

  @Override
  public String toString() {
    return "(x=" + column + ", y=" + row + ")";
  }
}