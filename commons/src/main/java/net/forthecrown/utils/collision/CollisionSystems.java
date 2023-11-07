package net.forthecrown.utils.collision;

import static net.forthecrown.utils.PluginUtil.getCallingPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class CollisionSystems {
  private CollisionSystems() {}

  public static <T> PlayerCollisionSystem<T> createSystem(CollisionListener<Player, T> listener) {
    var plugin = getCallingPlugin();
    return new PlayerCollisionSystem<>(new WorldChunkMap<>(), plugin, listener);
  }

  public static <T> PlayerCollisionSystem<T> createSystem(
      CollisionLookup<T> map,
      CollisionListener<Player, T> listener
  ) {
    var plugin = getCallingPlugin();
    return new PlayerCollisionSystem<>(map, plugin, listener);
  }

  public static <T> EntityCollisionSystem<T> createEntitySystem(CollisionListener<Entity, T> listener) {
    var plugin = getCallingPlugin();
    return new EntityCollisionSystem<>(new WorldChunkMap<>(), plugin, listener);
  }

  public static <T> EntityCollisionSystem<T> createEntitySystem(
      CollisionLookup<T> map,
      CollisionListener<Entity, T> listener
  ) {
    var plugin = getCallingPlugin();
    return new EntityCollisionSystem<>(map, plugin, listener);
  }
}
