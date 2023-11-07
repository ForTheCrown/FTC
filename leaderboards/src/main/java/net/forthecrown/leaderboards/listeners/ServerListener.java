package net.forthecrown.leaderboards.listeners;

import net.forthecrown.events.EarlyShutdownEvent;
import net.forthecrown.leaderboards.LeaderboardPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerListener implements Listener {

  private final LeaderboardPlugin plugin;

  public ServerListener(LeaderboardPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    plugin.reload();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEarlyShutdown(EarlyShutdownEvent event) {
    plugin.getService().save();
    plugin.getService().clear();
  }
}
