package net.forthecrown.waypoint;

import static net.forthecrown.waypoint.WaypointScan.Result.CANNOT_BE_DESTROYED;
import static net.forthecrown.waypoint.WaypointScan.Result.DESTROYED;
import static net.forthecrown.waypoint.WaypointScan.Result.NO_RESIDENTS_NAME_GUILD;
import static net.forthecrown.waypoint.WaypointScan.Result.POLE_BROKEN;
import static net.forthecrown.waypoint.WaypointScan.Result.SUCCESS;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.apache.logging.log4j.Logger;

public final class WaypointScan {
  private WaypointScan() {}

  private static final Logger LOGGER = FTC.getLogger();

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
    ) {
      LOGGER.debug("scan={} is invulnerable", waypoint);
      return CANNOT_BE_DESTROYED;
    }

    if (waypoint.getType().isDestroyed(waypoint)) {
      LOGGER.debug("scan={} is destroyed", waypoint);
      return DESTROYED;
    }

    if (waypoint.getResidents().isEmpty()
        && waypoint.get(WaypointProperties.GUILD_OWNER) == null
        && Strings.isNullOrEmpty(waypoint.get(WaypointProperties.NAME))
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