package net.forthecrown.waypoints.listeners;

import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.waypoints.WaypointManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class DayChangeListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    WaypointManager manager = WaypointManager.getInstance();
    manager.onDayChange();
  }
}
