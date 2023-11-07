package net.forthecrown.utils.collision;

import java.util.Objects;
import lombok.Getter;
import net.forthecrown.events.Events;
import net.forthecrown.utils.math.Bounds3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class CollisionSystem<S extends Entity, T> {

  @Getter
  private final CollisionLookup<T> map;

  @Getter
  private final Plugin plugin;

  @Getter
  private final CollisionListener<S, T> listener;

  private final CollisionSet<T> collisionBuffer;

  private Listener eventListener;

  @Getter
  private boolean listenerRegistered = false;

  protected CollisionSystem(
      CollisionLookup<T> map,
      Plugin plugin,
      CollisionListener<S,T> listener
  ) {
    Objects.requireNonNull(map, "Null map");
    Objects.requireNonNull(plugin, "Null plugin");
    Objects.requireNonNull(listener, "Null listener");

    this.map = map;
    this.plugin = plugin;
    this.listener = listener;

    this.collisionBuffer = new CollisionHashSet<>(50);
  }

  public void beginListening() {
    if (listenerRegistered) {
      return;
    }

    if (eventListener == null) {
      eventListener = makeListener();
    }

    var pl = Bukkit.getPluginManager();
    pl.registerEvents(eventListener, plugin);
  }

  public void stopListening() {
    if (!listenerRegistered || eventListener == null) {
      return;
    }

    Events.unregister(eventListener);
    listenerRegistered = false;
  }

  protected abstract Listener makeListener();

  public void run(S source, Location from, Location to, boolean teleport) {
    Bounds3i sourceBounds = makeSourceBounds(source, from);
    Bounds3i destBounds = makeSourceBounds(source, to);

    World sourceWorld = from.getWorld();
    World destWorld = to.getWorld();

    if (!teleport && Objects.equals(sourceWorld, destWorld)) {
      var combined = sourceBounds.combine(destBounds);
      map.getColliding(sourceWorld, combined, collisionBuffer);
    } else {
      map.getColliding(sourceWorld, sourceBounds, collisionBuffer);
      map.getColliding(destWorld, destBounds, collisionBuffer);
    }

    if (collisionBuffer.isEmpty()) {
      return;
    }

    for (Collision<T> collision : collisionBuffer) {
      boolean sourceInside = collision.isColliding(sourceWorld, sourceBounds);
      boolean destInside = collision.isColliding(destWorld, destBounds);

      var value = collision.value();

      if (sourceInside && destInside) {
        listener.onMoveInside(source, value);
      } else if (sourceInside) {
        listener.onExit(source, value);
      } else if (destInside) {
        listener.onEnter(source, value);
      }
    }

    collisionBuffer.clear();
  }

  protected Bounds3i makeSourceBounds(S source, Location l) {
    double halfWidth = source.getWidth() / 2;
    double height = source.getHeight();

    return Bounds3i.of(
        l.getX() - halfWidth,
        l.getY(),
        l.getZ() - halfWidth,

        l.getX() + halfWidth,
        l.getY() + height,
        l.getZ() + halfWidth
    );
  }
}
