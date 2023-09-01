package net.forthecrown.utils.collision;

import java.util.Objects;
import net.forthecrown.utils.math.Bounds3i;
import org.bukkit.World;

public record Collision<T>(
    T value,
    World collisionWorld,
    Bounds3i valueBounds
) {

  public boolean isColliding(World world, Bounds3i bounds3i) {
    return valueBounds.overlaps(bounds3i) && Objects.equals(collisionWorld, world);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Collision<?> collision)) {
      return false;
    }
    return Objects.equals(value, collision.value);
  }
}
