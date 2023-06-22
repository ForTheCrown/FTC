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

  /**
   * Removes a player from a cooldown
   * @param playerId The UUID of the player to remove from cooldown
   * @param category The category to remove from
   * @return True, if the player was in the category and was removed,
   *         false otherwise
   */
  boolean remove(@NotNull UUID playerId, @NotNull String category);

  default boolean onCooldown(@NotNull UUID playerId) {
    return onCooldown(playerId, TRANSIENT_CATEGORY);
  }

  /**
   * Tests if a player is on cooldown
   *
   * @param playerId The player's UUID
   * @param category The cooldown category
   * @return True, if the UUID is NOT on cooldown, false otherwise
   */
  boolean onCooldown(@NotNull UUID playerId, @NotNull String category);

  /**
   * Gets the remaining duration of a user's cooldown
   *
   * @param playerId The UUID of the player to get the remaining cooldown for
   * @param category The category to get the cooldown of
   *
   * @return Remaining duration. Returns null, if not on cooldown, and returns
   *         a duration with -1 seconds, if on never-ending cooldown
   */
  @Nullable
  Duration getRemainingTime(UUID playerId, String category);
}