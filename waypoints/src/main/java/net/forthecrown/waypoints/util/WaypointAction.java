package net.forthecrown.waypoints.util;

import net.forthecrown.waypoints.Waypoint;

public interface WaypointAction {

  void accept(Waypoint waypoint);

  void onFinish();
}
