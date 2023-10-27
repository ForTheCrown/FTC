package net.forthecrown.waypoints.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.forthecrown.Loggers;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import org.slf4j.Logger;

public class MultiHomeFixer implements Runnable {

  private static final Logger LOGGER = Loggers.getLogger();

  private final WaypointManager manager;

  public MultiHomeFixer(WaypointManager manager) {
    this.manager = manager;
  }

  @Override
  public void run() {
    Map<UUID, List<Waypoint>> residencies = new Object2ObjectOpenHashMap<>();

    for (Waypoint waypoint : manager.getWaypoints()) {
      for (UUID playerId : waypoint.getResidents().keySet()) {
        List<Waypoint> waypoints = residencies.computeIfAbsent(playerId, u -> new ArrayList<>());
        waypoints.add(waypoint);
      }
    }

    residencies.forEach((uuid, waypoints) -> {
      if (waypoints.size() <= 1) {
        return;
      }

      LOGGER.warn(
          "Player {} is a resident in more than 1 waypoint. Removing oldest from list. "
              + "residencies={}",
          uuid, waypoints
      );

      waypoints.sort((o1, o2) -> {
        long movein1 = o1.getResidents().getLong(uuid);
        long movein2 = o2.getResidents().getLong(uuid);
        return Long.compare(movein1, movein2);
      });

      int finalIndex = waypoints.size() - 1;

      for (int i = 0; i < waypoints.size(); i++) {
        Waypoint waypoint = waypoints.get(i);

        if (i == finalIndex) {
          break;
        }

        waypoint.removeResident(uuid);
      }
    });
  }
}
