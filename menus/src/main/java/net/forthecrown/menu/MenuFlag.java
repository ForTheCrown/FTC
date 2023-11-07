package net.forthecrown.menu;

public enum MenuFlag {
  /**
   * Allows items to be moved into a menu, unless explicitly denied by a menu
   * node
   */
  ALLOW_ITEM_MOVING,

  /**
   * Assigns each item placed in an inventory an auto-incremented 64-bit ID number that's
   * placed into the item's NBT to prevent it stacking
   * <p>
   * Useful for inventories that allow free item movement to any degree
   */
  PREVENT_ITEM_STACKING,

  /**
   * Allows shift-click events to move items into the inventory
   */
  ALLOW_SHIFT_CLICKING;
}