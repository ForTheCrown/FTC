package net.forthecrown.events;

import java.time.ZonedDateTime;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class DayChangeEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final ZonedDateTime time;

  public DayChangeEvent(ZonedDateTime time) {
    this.time = time;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
