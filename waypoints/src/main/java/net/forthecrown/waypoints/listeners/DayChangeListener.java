package net.forthecrown.waypoints.listeners;

import io.papermc.paper.util.Tick;
import java.util.HashMap;
import java.util.Map;
import net.forthecrown.Loggers;
import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.utils.Time;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointConfig;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.WaypointScan;
import net.forthecrown.waypoints.WaypointScan.Result;
import net.forthecrown.waypoints.util.DelayedWaypointIterator;
import net.forthecrown.waypoints.util.WaypointAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

class DayChangeListener implements Listener {

  public static final Logger LOGGER = Loggers.getLogger();

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    WaypointManager manager = WaypointManager.getInstance();

    boolean monthlyReset = event.getTime().getDayOfMonth() == 1;

    for (var w : manager.getWaypoints()) {
      w.set(WaypointProperties.VISITS_DAILY, 0);
      if (monthlyReset) {
        w.set(WaypointProperties.VISITS_MONTHLY, 0);
      }
    }

    DelayedWaypointIterator it = new DelayedWaypointIterator(
        manager.getWaypoints().iterator(),
        Tick.of(1),
        new RemovalAction(manager)
    );

    it.schedule();
  }

  private static class RemovalAction implements WaypointAction {

    private final Map<Waypoint, Result> removed = new HashMap<>();
    private final WaypointManager manager;
    private final WaypointConfig config;

    public RemovalAction(WaypointManager manager) {
      this.manager = manager;
      this.config = manager.config();
    }

    @Override
    public void accept(Waypoint waypoint) {
      if (!waypoint.getType().isBuildable()) {
        return;
      }

      Result result = WaypointScan.scan(waypoint);

      if (result == Result.SUCCESS || result == Result.CANNOT_BE_DESTROYED) {
        return;
      }

      if (result == Result.DESTROYED) {
        removed.put(waypoint, result);
        return;
      }

      // Residents empty, no set name, no guild or pole was broken
      if (shouldRemove(waypoint, config)) {
        removed.put(waypoint, result);
      }
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

    public void onFinish() {
      // No waypoints to remove so stop here
      if (removed.isEmpty()) {
        return;
      }

      // Remove all invalid waypoints
      removed.forEach((waypoint, result) -> {
        LOGGER.info("Auto-removing waypoint {}, reason={}",
            waypoint.identificationInfo(),
            result.getReason()
        );

        manager.removeWaypoint(waypoint);
      });
    }
  }
}
