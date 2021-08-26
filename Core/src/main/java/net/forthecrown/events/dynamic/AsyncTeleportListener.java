package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AsyncTeleportListener implements Listener {

    private final Player player;
    private final UserTeleport teleport;

    public AsyncTeleportListener(CrownUser user, UserTeleport teleport){
        this.player = user.getPlayer();
        this.teleport = teleport;

        Bukkit.getPluginManager().registerEvents(this, Crown.inst());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!event.getPlayer().equals(player)) return;
        if(!event.hasChangedBlock()) return;
        teleport.interrupt(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!event.getEntity().equals(player)) return;
        teleport.interrupt(false);
    }
}
