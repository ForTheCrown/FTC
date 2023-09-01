package net.forthecrown.worldloader;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

public class WorldLoadCompleteEvent extends WorldEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  @Getter
  private final boolean success;

  public WorldLoadCompleteEvent(@NotNull World world, boolean success) {
    super(world);
    this.success = success;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
