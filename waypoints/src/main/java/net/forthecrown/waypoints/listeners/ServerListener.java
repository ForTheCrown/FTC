package net.forthecrown.waypoints.listeners;

import net.forthecrown.events.EarlyShutdownEvent;
import net.forthecrown.waypoints.WaypointManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

class ServerListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onServerLoad(ServerLoadEvent event) {
    WaypointManager.getInstance().load();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEarlyShutdown(EarlyShutdownEvent event) {
    WaypointManager.getInstance().save();
  }
}
