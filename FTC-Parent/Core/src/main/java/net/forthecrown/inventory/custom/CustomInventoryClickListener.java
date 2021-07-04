package net.forthecrown.inventory.custom;

import net.forthecrown.core.chat.Announcer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

public class CustomInventoryClickListener implements Listener {

    @EventHandler
    public static void onPlayerClickItemInInv(InventoryClickEvent event) {
        Announcer.debug(1);
        if(!(event.getInventory().getHolder() instanceof CustomInventory)) return;
        if(event.isShiftClick()) event.setCancelled(true);

        Announcer.debug(11);
        // Don't handle click in the player's own inventory
        if (event.getClickedInventory() instanceof PlayerInventory) return;

        Announcer.debug(111);
        event.setCancelled(true);
        if(event.getCurrentItem() == null) return;

        Announcer.debug(1111);
        CustomInventory clickedInv = (CustomInventory) event.getInventory().getHolder();
        clickedInv.handleClick(event.getWhoClicked(), event.getSlot());
    }

}
