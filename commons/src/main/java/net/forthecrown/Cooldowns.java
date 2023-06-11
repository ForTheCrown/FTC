package net.forthecrown;

import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Cooldowns {

  String TRANSIENT_CATEGORY = "transient";

  long NO_END_COOLDOWN = -1L;

  static Cooldowns cooldowns() throws IllegalStateException {
    return ServiceInstances.getCooldown();
  }

  Set<String> getExistingCategories();

  default void cooldown(@NotNull UUID playerId, @NotNull Duration time) {
    cooldown(playerId, TRANSIENT_CATEGORY, time);
  }

  void cooldown(@NotNull UUID playerId, @NotNull String category, @NotNull Duration time);

  default void cooldown(@NotNull UUID playerId, @NotNull String category, long durationTicks) {
    cooldown(playerId, category, Tick.of(durationTicks));
  }

  default boolean remove(UUID playerId) {
    return remove(playerId, TRANSIENT_CATEGORY);
  }

  boolean remove(@NotNull UUID playerId, @NotNull String category);

  default boolean onCooldown(@NotNull UUID playerId) {
    return onCooldown(playerId, TRANSIENT_CATEGORY);
  }

  boolean onCooldown(@NotNull UUID playerId, @NotNull String category);

  @Nullable
  Duration getRemainingTime(UUID playerId, String category);
}