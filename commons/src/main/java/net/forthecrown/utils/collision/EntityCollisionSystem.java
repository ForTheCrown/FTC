package net.forthecrown.utils.collision;

import io.papermc.paper.event.entity.EntityMoveEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.plugin.Plugin;

public class EntityCollisionSystem<T> extends CollisionSystem<Entity, T> {

  protected EntityCollisionSystem(
      CollisionLookup<T> map,
      Plugin plugin,
      CollisionListener<Entity, T> listener
  ) {
    super(map, plugin, listener);
  }

  @Override
  protected Listener makeListener() {
    return new EntityListener(this);
  }
}

@RequiredArgsConstructor
class EntityListener implements Listener {

  private final EntityCollisionSystem<?> system;

  @EventHandler(ignoreCancelled = true)
  public void onPlayerMove(EntityMoveEvent event) {
    if (!event.hasChangedPosition()) {
      return;
    }

    system.run(event.getEntity(), event.getFrom(), event.getTo(), false);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerTeleport(EntityTeleportEvent event) {
    system.run(event.getEntity(), event.getFrom(), event.getTo(), true);
  }
}