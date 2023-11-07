package net.forthecrown.worldloader;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class WorldResetEvent extends WorldEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  /**
   * The seed the new world will use.
   * <p>
   * To get the seed of the old world use {@link World#getSeed()} from {@link #getWorld()} instead
   * of this, as this may be null.
   * <p>
   * If the value of this is null, it means a random seed will be generated later for the world
   */
  private Long seed;

  public WorldResetEvent(@NotNull World world) {
    super(world);
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
