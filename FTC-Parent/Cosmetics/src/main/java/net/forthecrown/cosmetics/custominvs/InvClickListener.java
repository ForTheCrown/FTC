package net.forthecrown.cosmetics.custominvs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class InvClickListener implements Listener {

    @EventHandler
    public static void onPlayerClickItemInInv(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof CustomInv)) return;
        event.setCancelled(true);

        // Don't handle click in the player's own inventory
        if (event.getClickedInventory() instanceof PlayerInventory) return;

        CustomInv clickedInv = (CustomInv) event.getInventory().getHolder();
        clickedInv.handleClick(event.getWhoClicked(), event.getSlot());
    }

}
