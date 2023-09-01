package net.forthecrown.events;

import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired before any actual FTC modules are disabled that will allow actions to
 * be performed before any plugins are unloaded to prevent class loading errors, because shutdown
 * events are too much for Paper or Spigot to make
 *
 */
public class EarlyShutdownEvent extends ServerEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
