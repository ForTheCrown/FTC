package net.forthecrown.utils.inventory.menu;

import net.forthecrown.utils.context.Context;

/**
 * Callback for menus which is triggered when a user clicks outside of a menu's
 * inventory while a menu is open
 */
public interface ExternalClickConsumer {
  void onShiftClick(ExternalClickContext click, Context context);
}