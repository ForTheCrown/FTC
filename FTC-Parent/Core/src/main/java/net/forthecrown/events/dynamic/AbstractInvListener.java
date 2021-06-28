package net.forthecrown.events.dynamic;

import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public abstract class AbstractInvListener implements Listener {

    protected final Player player;
    protected final CrownUser user;

    public AbstractInvListener(Player player) {
        this.player = player;
        this.user = UserManager.getUser(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!event.getPlayer().equals(player)) return;
        if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        HandlerList.unregisterAll(this);
    }
}
