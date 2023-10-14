package net.forthecrown.waypoints;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.user.User;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.math.vector.Vector3i;

public interface WMessages {


  Component HOME_WAYPOINT_SET = text(
      """
      Home waypoint set.
      Use /invite <player> to invite others\s
      Use /home to come to this waypoint when near another waypoint.
      """.trim(),

      NamedTextColor.YELLOW
  );

  static Component senderInvited(User target) {
    return format("Invited &e{0, user}&r.",
        NamedTextColor.GOLD, target
    );
  }

  static Component targetInvited(User sender) {
    return format("&e{0, user}&r has invited you to their region.",
        NamedTextColor.GOLD, sender
    );
  }

  static Component invitedTotal(int count) {
    return format("Invited a total of &e{0, number}&r people.",
        NamedTextColor.GOLD, count
    );
  }

  static Component createdWaypoint(Vector3i pos, WaypointType type) {
    String typeStr = "Waypoint";

    if (type == WaypointTypes.REGION_POLE) {
      typeStr = "Region pole";
    } else if (type == WaypointTypes.ADMIN) {
      typeStr = "Admin waypoint";
    }

    return format("Created &e{0}&r at x&6{1}&r y&6{2}&r z&6{3}&r.",
        NamedTextColor.GRAY,

        typeStr,
        pos.x(), pos.y(), pos.z()
    );
  }

}