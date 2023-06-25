package net.forthecrown.waypoints.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
@RequiredArgsConstructor
public class WaypointNameValidateEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final String name;

  private boolean allowed;

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}