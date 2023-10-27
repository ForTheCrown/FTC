package net.forthecrown.waypoints;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.forthecrown.user.User;

public class WaypointHomes {

  private static final Map<UUID, UUID> playerId2Home = new Object2ObjectOpenHashMap<>();

  public static Optional<Waypoint> getHome(User user) {
    Objects.requireNonNull(user, "Null user");
    return getWaypoint(user.getUniqueId());
  }

  public static Optional<Waypoint> getWaypoint(UUID playerId) {
    Objects.requireNonNull(playerId, "Null playerId");

    return Optional.ofNullable(playerId2Home.get(playerId))
        .map(WaypointManager.getInstance()::get);
  }

  public static void setHome(UUID playerId, Waypoint waypoint) {
    Objects.requireNonNull(playerId, "Null playerId");

    if (waypoint == null) {
      playerId2Home.remove(playerId);
    } else {
      playerId2Home.put(playerId, waypoint.getId());
    }
  }

  public static void setHome(User user, Waypoint waypoint) {
    Objects.requireNonNull(user, "Null user");
    setHome(user.getUniqueId(), waypoint);
  }
}
