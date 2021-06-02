package net.forthecrown.vikings.valhalla.active;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TriggerListener implements Listener {
    private final TriggerContainer container;

    public TriggerListener(TriggerContainer container) {
        this.container = container;
    }

    public boolean goodPlayer(Player player){
        return container.raid.getParty().contains(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!goodPlayer(event.getPlayer())) return;

        RaidCell cell = container.cellAt(event.getTo());
        container.attemptEventExecution(cell, container.moveEvents, event, event.getPlayer());
    }
}
