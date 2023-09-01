package net.forthecrown.waypoints.listeners;

import static net.forthecrown.events.Events.register;

public final class WaypointsListeners {
  private WaypointsListeners() {}

  public static void registerAll() {
    register(new WaypointDestroyListener());
    register(new WaypointListener());
    register(new PlayerJoinListener());
    register(new DayChangeListener());
    register(new ServerLoadListener());
    register(new HomeListener());
  }
}