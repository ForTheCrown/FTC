package net.forthecrown.waypoints.listeners;

import java.util.Optional;
import net.forthecrown.user.event.HomeCommandEvent;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointHomes;
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

    Optional<Waypoint> home = WaypointHomes.getHome(user);

    if (home.isEmpty()) {
      return;
    }

    event.setCancelled(true);
    WaypointVisit.visit(user, home.get());
  }
}
