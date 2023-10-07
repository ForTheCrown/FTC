package net.forthecrown.menu.internal;

import net.forthecrown.Loggers;
import net.forthecrown.menu.MenuFlag;
import net.forthecrown.menu.MenuHolder;
import net.forthecrown.menu.Slot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.PlayerInventory;
import org.slf4j.Logger;

public class MenuListener implements Listener {

  private static final Logger LOGGER = Loggers.getLogger();

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    LOGGER.debug("InventoryClickEvent, holder={}",
        event.getView().getTopInventory().getHolder()
    );

    if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      LOGGER.debug("Not a menu click");
      return;
    }

    var menu = holder.getMenu();
    var clicked = event.getClickedInventory();

    if (clicked == null || clicked instanceof PlayerInventory) {
      LOGGER.debug("Click was not inside menu");

      menu.onExternalClick(event);
      return;
    }

    LOGGER.debug("Click was inside menu");
    menu.onMenuClick(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent event) {
    LOGGER.debug("inventoryClose, holder={}", event.getView().getTopInventory().getHolder());

    if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }

    var menu = holder.getMenu();
    menu.onMenuClose(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    LOGGER.debug("InventoryDragEvent, holder={}", event.getView().getTopInventory().getHolder());

    if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }

    var menu = holder.getMenu();
    var view = event.getView();

    var slots = event.getRawSlots();
    LOGGER.debug("slots={}", slots.stream().map(Slot::of).toList());

    for (var i: event.getRawSlots()) {
      var inv = view.getInventory(i);

      if (inv == null || !(inv.getHolder() instanceof MenuHolder)) {
        continue;
      }

      if (!menu.hasFlag(MenuFlag.ALLOW_ITEM_MOVING)) {
        LOGGER.debug("Cancelling drag event");
        event.setCancelled(true);

        return;
      }
    }
  }
}