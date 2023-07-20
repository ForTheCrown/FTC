package net.forthecrown.waypoints.listeners;

import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.type.WaypointTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

class ServerLoadListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onServerLoad(ServerLoadEvent event) {
    WaypointTypes.REGISTRY.freeze();
    WaypointProperties.REGISTRY.freeze();

    WaypointManager.getInstance().load();
  }
}
