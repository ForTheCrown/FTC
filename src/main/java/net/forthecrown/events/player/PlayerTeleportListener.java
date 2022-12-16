package net.forthecrown.events.player;

import net.forthecrown.user.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerTeleportListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }

        var user = Users.get(event.getPlayer());

        if (!user.isTeleporting()) {
            return;
        }

        user.getLastTeleport().interrupt();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        var user = Users.get(event.getPlayer());

        if (!user.isTeleporting()) {
            return;
        }

        user.getLastTeleport().interrupt();
    }
}