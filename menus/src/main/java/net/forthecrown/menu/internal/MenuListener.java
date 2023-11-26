package net.forthecrown.menu.internal;

import net.forthecrown.Loggers;
import net.forthecrown.menu.MenuFlag;
import net.forthecrown.menu.MenuHolder;
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
    if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }

    var menu = holder.getMenu();
    var clicked = event.getClickedInventory();

    if (clicked == null || clicked instanceof PlayerInventory) {
      menu.onExternalClick(event);
      return;
    }

    LOGGER.debug("Click was inside menu");
    menu.onMenuClick(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }

    var menu = holder.getMenu();
    menu.onMenuClose(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }

    var menu = holder.getMenu();
    var view = event.getView();

    for (var i: event.getRawSlots()) {
      var inv = view.getInventory(i);

      if (inv == null || !(inv.getHolder() instanceof MenuHolder)) {
        continue;
      }

      if (!menu.hasFlag(MenuFlag.ALLOW_ITEM_MOVING)) {
        event.setCancelled(true);
        return;
      }
    }
  }
}