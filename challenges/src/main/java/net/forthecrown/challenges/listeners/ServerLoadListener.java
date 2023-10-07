package net.forthecrown.challenges.listeners;

import net.forthecrown.challenges.ChallengesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

class ServerLoadListener implements Listener {

  private final ChallengesPlugin plugin;

  public ServerLoadListener(ChallengesPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    plugin.load();
  }
}
