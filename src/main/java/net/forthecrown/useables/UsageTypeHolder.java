package net.forthecrown.useables;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  default <T extends UsageTypeHolder> @Nullable T asOrNull(@NotNull Class<T> clazz) {
    return as(clazz).orElse(null);
  }
}