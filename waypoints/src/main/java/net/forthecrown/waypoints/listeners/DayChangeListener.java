package net.forthecrown.waypoints.listeners;

import java.util.HashMap;
import java.util.Map;
import net.forthecrown.Loggers;
import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.utils.Time;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointConfig;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointScan;
import net.forthecrown.waypoints.WaypointScan.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

class DayChangeListener implements Listener {

  public static final Logger LOGGER = Loggers.getLogger();

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    WaypointManager manager = WaypointManager.getInstance();
    WaypointConfig config = manager.config();
    Map<Waypoint, Result> toRemove = new HashMap<>();

    for (var w : manager.getWaypoints()) {
      Result result = WaypointScan.scan(w);

      if (result == Result.SUCCESS || result == Result.CANNOT_BE_DESTROYED) {
        continue;
      }

      if (result == Result.DESTROYED) {
        toRemove.put(w, result);
        continue;
      }

      // Residents empty, no set name, no guild or pole was broken
      if (shouldRemove(w, config)) {
        toRemove.put(w, result);
      }
    }

    // No waypoints to remove so stop here
    if (toRemove.isEmpty()) {
      return;
    }

    // Remove all invalid waypoints
    toRemove.forEach((waypoint, result) -> {
      LOGGER.info("Auto-removing waypoint {}, reason={}",
          waypoint.identificationInfo(),
          result.getReason()
      );

      manager.removeWaypoint(waypoint);
    });
  }

  private boolean shouldRemove(Waypoint waypoint, WaypointConfig config) {
    if (waypoint.getLastValidTime() == -1) {
      waypoint.setLastValidTime(System.currentTimeMillis());
      return false;
    }

    long deletionDelay = config.waypointDeletionDelay.toMillis();
    long deletionTime = waypoint.getLastValidTime() + deletionDelay;

    return Time.isPast(deletionTime);
  }
}
