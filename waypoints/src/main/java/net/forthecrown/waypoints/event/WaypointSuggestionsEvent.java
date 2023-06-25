package net.forthecrown.waypoints.event;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class WaypointSuggestionsEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final CommandContext<CommandSource> context;
  private final SuggestionsBuilder builder;

  public WaypointSuggestionsEvent(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    this.context = context;
    this.builder = builder;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}