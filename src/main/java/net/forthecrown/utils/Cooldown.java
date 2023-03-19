package net.forthecrown.utils;

import static net.forthecrown.core.Cooldowns.NO_END_COOLDOWN;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnegative;
import net.forthecrown.core.Cooldowns;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A class that helps to add cooldowns to things.
 * @deprecated Use {@link Cooldowns} directly. This class is no longer supported
 */
@Deprecated
public final class Cooldown {
  private Cooldown() {}

  /**
   * The general category, literally just "general"
   */
  public static final String GENERAL = Cooldowns.TRANSIENT_CATEGORY;

  /**
   * Checks whether the sender is in the {@link Cooldown#GENERAL} category
   *
   * @param sender The sender to check for
   * @return If the sender is on {@link Cooldown#GENERAL} cooldown
   */
  public static boolean contains(@NotNull Audience sender) {
    return contains(sender, GENERAL);
  }

  /**
   * Checks whether the sender is on cooldown in the given category
   *
   * @param sender   The sender to check
   * @param category The category to check
   * @return If the sender is on cooldown for the given category
   */
  public static boolean contains(@NotNull Audience sender,
                                 @NotNull String category
  ) {
    return Cooldowns.getCooldowns().isOnCooldown(getUniqueId(sender), category);
  }

  /**
   * Same as {@link Cooldown#containsOrAdd(Audience, String, int)} except for
   * {@link Cooldown#GENERAL}
   *
   * @param sender The sender to check
   * @param ticks  The duration of the cooldown
   * @return Same as {@link Cooldown#containsOrAdd(Audience, String, int)}
   */
  public static boolean containsOrAdd(Audience sender,
                                      @Nonnegative int ticks
  ) {
    return containsOrAdd(sender, GENERAL, ticks);
  }

  /**
   * Checks whether the given category contains the given sender, if it does, returns true, else
   * adds sender to category for the given duration and returns false
   *
   * @param sender   The sender to check
   * @param category The category to check and potentially add to
   * @param ticks    The duration of the cooldown
   * @return True if category contains sender, false if it doesn't, if false, adds sender to
   * category
   */
  public static boolean containsOrAdd(Audience sender,
                                      @NotNull String category,
                                      @Nonnegative int ticks
  ) {
    boolean contains = contains(sender, category);

    // Sender is not in the map, add them lol
    if (!contains) {
      add(sender, category, ticks);
    }

    return contains;
  }

  /**
   * Adds the given sender to the {@link Cooldown#GENERAL} cooldown for the given ticks
   *
   * @param sender      The sender to add
   * @param timeInTicks The time to add them for
   */
  public static void add(@NotNull Audience sender,
                         @Nonnegative int timeInTicks
  ) {
    add(sender, GENERAL, timeInTicks);
  }

  /**
   * Adds the given sender into the {@link Cooldown#GENERAL} category for forever
   *
   * @param sender The sender to add
   */
  public static void add(@NotNull Audience sender) {
    add(sender, GENERAL, NO_END_COOLDOWN);
  }

  /**
   * Adds the given sender into the given category for forever
   *
   * @param sender   The sender to add
   * @param category The category to add them to
   */
  public static void add(@NotNull Audience sender,
                         @NotNull String category
  ) {
    add(sender, category, NO_END_COOLDOWN);
  }

  /**
   * Adds the given sender into the given category for the given duration
   *
   * @param sender      The sender to add
   * @param category    The category to add the sender to
   * @param timeInTicks The cooldown's duration
   */
  public static void add(@NotNull Audience sender,
                         @NotNull String category,
                         long timeInTicks
  ) {
    var id = getUniqueId(sender);
    long time = toMillis(timeInTicks);

    var cds = Cooldowns.getCooldowns();

    if (cds.isOnCooldown(id, category)) {
      return;
    }

    cds.cooldown(id, category, time);
  }

  /**
   * Removes the given sender from any cooldown
   *
   * @param sender The sender to remove
   */
  public static void remove(@NotNull Audience sender) {
    remove(sender, GENERAL);
  }

  /**
   * Removes the given sender from the given cooldown category
   *
   * @param sender   The sender to remove
   * @param category The category to remove the sender from
   */
  public static void remove(@NotNull Audience sender, @NotNull String category) {
    UUID uuid = getUniqueId(sender);
    Cooldowns.getCooldowns().remove(uuid, category);
  }

  public static void testAndThrow(Audience audience, String category, long ticks)
      throws CommandSyntaxException
  {
    long time = toMillis(ticks);
    var id = getUniqueId(audience);

    Cooldowns.getCooldowns().testAndThrow(id, category, time);
  }

  private static UUID getUniqueId(Audience audience) {
    Objects.requireNonNull(audience);

    if (audience instanceof Player player) {
      return player.getUniqueId();
    } else if (audience instanceof User user) {
      return user.getUniqueId();
    } else if (audience instanceof CommandSource source && source.isPlayer()) {
      return source.asEntityOrNull().getUniqueId();
    }

    throw Util.newException("Audience '%s' is not a player, nor a user");
  }

  private static long toMillis(long ticks) {
    if (ticks == NO_END_COOLDOWN) {
      return NO_END_COOLDOWN;
    }

    return Time.ticksToMillis(ticks);
  }
}