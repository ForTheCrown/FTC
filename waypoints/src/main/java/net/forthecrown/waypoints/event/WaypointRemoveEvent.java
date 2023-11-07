package net.forthecrown.waypoints.event;

import lombok.Getter;
import net.forthecrown.waypoints.Waypoint;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class WaypointRemoveEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final Waypoint waypoint;

  public WaypointRemoveEvent(Waypoint waypoint) {
    this.waypoint = waypoint;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}