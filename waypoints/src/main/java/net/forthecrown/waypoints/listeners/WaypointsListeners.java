package net.forthecrown.waypoints.listeners;

import static net.forthecrown.events.Events.register;

public final class WaypointsListeners {
  private WaypointsListeners() {}

  public static void registerAll() {
    register(new DayChangeListener());
    register(new HomeListener());
    register(new PlayerJoinListener());
    register(new ServerListener());
    register(new WaypointDestroyListener());
    register(new WaypointListener());
  }
}