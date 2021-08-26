package net.forthecrown.events;

import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.InventoryCloseAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;

public class InventoryBuilderListener implements Listener {

    //Yea ngl Wout, your listener for the inventory stuff was better.
    //For this scenario, having a constantly registered listener would be better
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof BuiltInventory)) return;
        if(event.isShiftClick()) event.setCancelled(true);
        if (event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);

        BuiltInventory inventory = (BuiltInventory) event.getInventory().getHolder();
        inventory.run((Player) event.getWhoClicked(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!(event.getInventory().getHolder() instanceof BuiltInventory)) return;

        BuiltInventory inventory = (BuiltInventory) event.getInventory().getHolder();
        InventoryCloseAction action = inventory.getOnClose();

        if(action == null) return;
        action.onClose((Player) event.getPlayer(), event.getReason());
    }
}
