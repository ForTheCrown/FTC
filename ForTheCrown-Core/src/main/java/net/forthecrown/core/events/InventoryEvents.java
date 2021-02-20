package net.forthecrown.core.events;

import net.forthecrown.core.inventories.CustomInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class InventoryEvents implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getView().getTopInventory().getHolder() == null) return;
        if(!(event.getView().getTopInventory().getHolder() instanceof CustomInventory)) return;
        if(event.isShiftClick()) event.setCancelled(true);
        if(event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);

        for (String s: CustomInventory.CUSTOM_INVENTORIES.keySet()){
            if(event.getView().getTitle().contains(s)) CustomInventory.CUSTOM_INVENTORIES.get(s).onInventoryClick(event);
        }
    }

    /*
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getView().getTopInventory().getHolder() == null) return;
        if(!(event.getView().getTopInventory().getHolder() instanceof CustomInventory)) return;

        for (String s: CustomInventory.CUSTOM_INVENTORIES.keySet()){
            if(event.getView().getTitle().contains(s)) CustomInventory.CUSTOM_INVENTORIES.get(s).onInventoryClose(event);
        }
    }*/
}
