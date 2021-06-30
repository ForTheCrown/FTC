package net.forthecrown.events.dynamic;

import net.forthecrown.core.inventory.builder.BuiltInventory;
import net.forthecrown.core.inventory.builder.InventoryAction;
import net.forthecrown.user.UserManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryBuilderListener implements Listener {

    private final BuiltInventory inventory;
    private final Player player;

    public InventoryBuilderListener(BuiltInventory inventory, Player player) {
        this.inventory = inventory;
        this.player = player;
    }

    private boolean isntPlayer(Entity entity){
        return !entity.equals(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(isntPlayer(event.getWhoClicked())) return;

        inventory.run(player, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(isntPlayer(event.getPlayer())) return;

        HandlerList.unregisterAll(this);

        InventoryAction action = inventory.getOnClose();
        if(action == null) return;
        action.run(UserManager.getUser(player));
    }
}
