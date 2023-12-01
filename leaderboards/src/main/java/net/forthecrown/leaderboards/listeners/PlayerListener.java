package net.forthecrown.leaderboards.listeners;

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import lombok.RequiredArgsConstructor;
import net.forthecrown.leaderboards.LeaderboardPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

  private final LeaderboardPlugin plugin;

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();

    player.getScheduler().runDelayed(plugin, scheduledTask -> {
      plugin.getService().getTriggers().updateFor(player);
    }, null, 20);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Player player = event.getPlayer();

    player.getScheduler().runDelayed(plugin, scheduledTask -> {
      plugin.getService().getTriggers().updateFor(player);
    }, null, 20);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerTeleportEndGateway(PlayerTeleportEndGatewayEvent event) {
    onPlayerTeleport(event);
  }
}
