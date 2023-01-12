package net.forthecrown.utils.inventory.menu;

public enum MenuFlag {
  /**
   * Allows items to be moved into a menu, unless explicitly denied by a menu
   * node
   */
  ALLOW_ITEM_MOVING,

  /**
   * Assigns each item placed in an inventory an auto-incremented long ID to
   * prevent the item from being stacked.
   * <p>
   * Useful for inventories that allow free item movement to any degree
   */
  PREVENT_ITEM_STACKING,

  /**
   * Allows shift-click events to move items into the inventory
   */
  ALLOW_SHIFT_CLICKING;
}