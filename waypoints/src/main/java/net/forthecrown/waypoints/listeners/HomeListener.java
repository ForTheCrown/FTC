package net.forthecrown.waypoints.listeners;

import net.forthecrown.user.event.HomeCommandEvent;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.visit.WaypointVisit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;

public class HomeListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onHomeCommand(HomeCommandEvent event) {
    if (event.isNameSet()) {
      return;
    }

    var user = event.getUser();
    BoundingBox playerBounds = user.getPlayer().getBoundingBox();
    Waypoint waypoint = Waypoints.getColliding(user.getPlayer());

    if (waypoint == null || !waypoint.getBounds().overlaps(playerBounds)) {
      return;
    }

    Waypoint home = Waypoints.getHomeWaypoint(user);

    if (home == null) {
      return;
    }

    event.setCancelled(true);
    WaypointVisit.visit(user, home);
  }
}
