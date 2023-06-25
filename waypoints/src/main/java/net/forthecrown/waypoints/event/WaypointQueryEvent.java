package net.forthecrown.waypoints.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.waypoints.Waypoint;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter @Setter @RequiredArgsConstructor
public class WaypointQueryEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final String query;

  private Waypoint result;

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}