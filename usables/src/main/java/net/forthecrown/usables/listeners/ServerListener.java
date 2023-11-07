package net.forthecrown.usables.listeners;

import net.forthecrown.events.EarlyShutdownEvent;
import net.forthecrown.usables.UsablesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

class ServerListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    UsablesPlugin plugin = UsablesPlugin.get();
    plugin.freezeRegistries();
    plugin.reload();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEarlyShutdown(EarlyShutdownEvent event) {
    UsablesPlugin plugin = UsablesPlugin.get();
    plugin.save();
  }
}
