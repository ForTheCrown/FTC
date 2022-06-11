package net.forthecrown.inventory.builder;

import net.forthecrown.inventory.FtcInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * An action ran when an inventory is closed
 */
public interface InventoryCloseAction {
    void onClose(Player player, FtcInventory inventory, InventoryCloseEvent.Reason reason);
}