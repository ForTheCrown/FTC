package net.forthecrown.waypoints;

import static net.forthecrown.waypoints.WaypointScan.Result.CANNOT_BE_DESTROYED;
import static net.forthecrown.waypoints.WaypointScan.Result.DESTROYED;
import static net.forthecrown.waypoints.WaypointScan.Result.NO_RESIDENTS_NAME_GUILD;
import static net.forthecrown.waypoints.WaypointScan.Result.POLE_BROKEN;
import static net.forthecrown.waypoints.WaypointScan.Result.SUCCESS;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.waypoints.type.WaypointTypes;
import org.slf4j.Logger;

public final class WaypointScan {
  private WaypointScan() {}

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  @RequiredArgsConstructor
  public enum Result {
    SUCCESS ("Success", false),
    POLE_BROKEN ("Pole broken", false),
    CANNOT_BE_DESTROYED ("Cannot be destroyed", false),
    DESTROYED ("Destroyed", true),
    NO_RESIDENTS_NAME_GUILD ("No residents/name/guild", true);

    final String reason;
    final boolean removable;
  }

  public static Result scan(Waypoint waypoint) {
    if (waypoint.getType() == WaypointTypes.ADMIN
        || waypoint.get(WaypointProperties.INVULNERABLE)
        || !waypoint.getType().isBuildable()
    ) {
      LOGGER.debug("scan={} is invulnerable", waypoint);
      return CANNOT_BE_DESTROYED;
    }

    if (waypoint.getType().isDestroyed(waypoint)) {
      LOGGER.debug("scan={} is destroyed", waypoint);
      return DESTROYED;
    }

    String name = waypoint.get(WaypointProperties.NAME);

    if (waypoint.getResidents().isEmpty()
        && Strings.isNullOrEmpty(name)
        && waypoint.getType().canBeRemoved(waypoint)
    ) {
      LOGGER.debug("scan={} has no residents/name/guild", waypoint);
      return NO_RESIDENTS_NAME_GUILD;
    }

    var test = waypoint.getType().isValid(waypoint);

    if (test.isPresent()) {
      LOGGER.debug("scan={} is broken", waypoint);
      return POLE_BROKEN;
    }

    LOGGER.debug("scan={} is perfect", waypoint);
    return SUCCESS;
  }
}