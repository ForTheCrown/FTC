package net.forthecrown.waypoints.event;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.waypoints.Waypoint;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WaypointParseEvent extends Event implements Cancellable {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final StringReader reader;

  @Getter
  private final int startCursor;

  @Getter @Setter
  private ParseResult<Waypoint> parseResult;

  @Setter @Getter
  private CommandSyntaxException exception;

  public WaypointParseEvent(StringReader reader) {
    this.reader = reader;
    this.startCursor = reader.getCursor();
  }

  public StringReader getReader() {
    return reader;
  }

  public StringReader getCopiedReader() {
    return Readers.copy(reader, startCursor);
  }

  @Override
  public boolean isCancelled() {
    return exception != null;
  }

  @Override
  public void setCancelled(boolean cancel) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}