package net.forthecrown.useables;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * A generic super class for both {@link CheckHolder} and {@link ActionHolder}
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
}