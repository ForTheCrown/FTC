package net.forthecrown.events;

import net.forthecrown.inventory.custom.CustomInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class CustomInventoryClickListener implements Listener {

    @EventHandler
    public static void onPlayerClickItemInInv(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof CustomInventory)) return;
        if(event.isShiftClick()) event.setCancelled(true);

        // Don't handle click in the player's own inventory
        if (event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);
        if(event.getCurrentItem() == null) return;

        CustomInventory clickedInv = (CustomInventory) event.getInventory().getHolder();
        clickedInv.handleClick(event.getWhoClicked(), event.getSlot());
    }

}
