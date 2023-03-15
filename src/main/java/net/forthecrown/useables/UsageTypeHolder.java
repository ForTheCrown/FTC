package net.forthecrown.useables;

import java.util.Optional;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A generic super class for both {@link CheckHolder} and {@link Usable}
 */
public interface UsageTypeHolder {

  default <T extends UsageTypeHolder> boolean is(@NotNull Class<T> clazz) {
    return clazz.isInstance(this);
  }

  default <T extends UsageTypeHolder> @NotNull Optional<T> as(@NotNull Class<T> clazz) {
    if (!is(clazz)) {
      return Optional.empty();
    }

    return Optional.of((T) this);
  }

  default World getWorld() {
    if (is(UsableBlock.class)) {
      return as(UsableBlock.class).orElseThrow().getWorld();
    }

    if (is(UsableEntity.class)) {
      return as(UsableEntity.class).orElseThrow().getEntity().getWorld();
    }

    if (is(UsableTrigger.class)) {
      return as((UsableTrigger.class)).orElseThrow().getBounds().getWorld();
    }

    return null;
  }
}