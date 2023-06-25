package net.forthecrown.waypoints.listeners;

import static net.forthecrown.events.Events.register;

public final class WaypointsListeners {
  private WaypointsListeners() {}

  public static void registerAll() {
    register(new WaypointDestroyListener());
    register(new WaypointListener());
  }
}