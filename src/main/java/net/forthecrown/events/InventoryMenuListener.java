package net.forthecrown.events;

import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.MenuFlag;
import net.forthecrown.utils.inventory.menu.MenuInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.PlayerInventory;

public class InventoryMenuListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) {
      return;
    }

    if (event.getClickedInventory() == null
        || event.getClickedInventory() instanceof PlayerInventory
    ) {
      menu.onExternalClick(event);
      return;
    }

    menu.onMenuClick(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) {
      return;
    }

    menu.onMenuClose(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) {
      return;
    }

    var view = event.getView();
    for (var i: event.getRawSlots()) {
      var inv = view.getInventory(i);

      if (inv instanceof MenuInventory) {
        event.setCancelled(!menu.hasFlag(MenuFlag.ALLOW_ITEM_MOVING));
        return;
      }
    }
  }
}