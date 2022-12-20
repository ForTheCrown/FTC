package net.forthecrown.waypoint;

import static net.forthecrown.waypoint.WaypointScan.Result.CANNOT_BE_DESTROYED;
import static net.forthecrown.waypoint.WaypointScan.Result.DESTROYED;
import static net.forthecrown.waypoint.WaypointScan.Result.NO_RESIDENTS_NAME_GUILD;
import static net.forthecrown.waypoint.WaypointScan.Result.POLE_BROKEN;
import static net.forthecrown.waypoint.WaypointScan.Result.SUCCESS;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.waypoint.type.WaypointTypes;

public class WaypointScan {

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

  public static boolean canBeRemoved(Waypoint waypoint) {
    return scan(waypoint).isRemovable();
  }

  public static Result scan(Waypoint waypoint) {
    if (waypoint.getType() == WaypointTypes.ADMIN
        || waypoint.get(WaypointProperties.INVULNERABLE)
    ) {
      return CANNOT_BE_DESTROYED;
    }

    if (waypoint.getType().isDestroyed(waypoint)) {
      return DESTROYED;
    }

    if (waypoint.getResidents().isEmpty()
        && waypoint.get(WaypointProperties.GUILD_OWNER) == null
        && Strings.isNullOrEmpty(waypoint.get(WaypointProperties.NAME))
    ) {
      return NO_RESIDENTS_NAME_GUILD;
    }

    var test = waypoint.getType().isValid(waypoint);

    return test.isPresent()
        ? POLE_BROKEN
        : SUCCESS;
  }
}