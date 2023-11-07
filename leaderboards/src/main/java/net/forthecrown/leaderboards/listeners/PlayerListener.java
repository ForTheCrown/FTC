package net.forthecrown.leaderboards.listeners;

import lombok.RequiredArgsConstructor;
import net.forthecrown.leaderboards.LeaderboardPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
}
