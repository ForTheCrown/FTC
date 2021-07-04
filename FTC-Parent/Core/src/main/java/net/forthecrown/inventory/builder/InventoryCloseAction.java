package net.forthecrown.inventory.builder;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface InventoryCloseAction {
    void onClose(Player player, InventoryCloseEvent.Reason reason);
}
