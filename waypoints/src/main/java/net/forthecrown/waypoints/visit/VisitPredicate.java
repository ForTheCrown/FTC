package net.forthecrown.waypoints.visit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.SyntaxExceptions;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.WaypointProperties;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

public interface VisitPredicate {

  VisitPredicate RIDING_VEHICLE = visit -> {
    Entity entity = visit.getUser().getPlayer().getVehicle();
    if (entity == null || entity instanceof Vehicle) {
      return;
    }

    throw WExceptions.ONLY_IN_VEHICLE;
  };

  VisitPredicate NOT_AT_SAME = visit -> {
    if (!visit.isNearWaypoint()) {
      return;
    }
    var near = visit.getNearestWaypoint();

    if (!near.equals(visit.getDestination())) {
      return;
    }

    throw Exceptions.create("Already at destination waypoint");
  };

  VisitPredicate IS_NEAR = visit -> {
    var player = visit.getUser();

    if (player.hasPermission(WPermissions.WAYPOINTS_ADMIN)) {
      return;
    }

    var nearest = visit.getNearestWaypoint();

    if (visit.isNearWaypoint()) {
      return;
    }

    if (nearest == null) {
      throw WExceptions.FAR_FROM_WAYPOINT;
    } else {
      throw WExceptions.farFromWaypoint(nearest);
    }
  };

  VisitPredicate DESTINATION_VALID = waypointIsValid(true);
  VisitPredicate NEAREST_VALID = waypointIsValid(false);

  /**
   * Tests if the visit is allowed to continue
   * <p></p>
   * Predicates are the first thing called when a region visit is ran
   *
   * @param visit The visit to check
   * @throws CommandSyntaxException If the check failed
   */
  void test(WaypointVisit visit) throws CommandSyntaxException;

  private static VisitPredicate waypointIsValid(boolean dest) {
    return visit -> {
      if (visit.getUser().hasPermission(WPermissions.WAYPOINTS_ADMIN)) {
        return;
      }

      if (dest && !visit.getDestination().isWorldLoaded()) {
        throw WExceptions.UNLOADED_WORLD;
      }

      var waypoint = dest
          ? visit.getDestination()
          : visit.getNearestWaypoint();

      if (!dest && !visit.isNearWaypoint()) {
        return;
      }

      // Should only happen if the nearest
      // waypoint is null, in the case of admins TPing
      // from worlds with no waypoints, which should be
      // checked by a preceding predicate
      if (waypoint == null || waypoint.get(WaypointProperties.INVULNERABLE)) {
        return;
      }

      var exc = waypoint.getType().isValid(waypoint)
          .map(e -> {
            Component msg = SyntaxExceptions.formatCommandException(e);

            // Prefix with either 'target' or 'nearest' to
            // make the message a bit more readable
            if (dest) {
              msg = Component.text("Target ").append(msg);
            } else {
              msg = Component.text("Nearest ").append(msg);
            }

            return Exceptions.create(msg);
          });

      if (exc.isEmpty()) {
        return;
      }

      throw exc.get();
    };
  }
}