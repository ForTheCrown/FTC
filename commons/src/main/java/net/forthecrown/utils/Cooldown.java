package net.forthecrown.utils;

import static net.forthecrown.Cooldowns.NO_END_COOLDOWN;
import static net.forthecrown.Cooldowns.TRANSIENT_CATEGORY;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnegative;
import net.forthecrown.Cooldowns;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Cooldown {

  /**
   * Checks whether the sender is in the {@link Cooldowns#TRANSIENT_CATEGORY} category
   *
   * @param sender The sender to check for
   * @return If the sender is on {@link Cooldowns#TRANSIENT_CATEGORY} cooldown
   */
  public static boolean contains(@NotNull Audience sender) {
    return contains(sender, TRANSIENT_CATEGORY);
  }

  /**
   * Checks whether the sender is on cooldown in the given category
   *
   * @param sender   The sender to check
   * @param category The category to check
   * @return If the sender is on cooldown for the given category
   */
  public static boolean contains(@NotNull Audience sender, @NotNull String category) {
    return Cooldowns.cooldowns().onCooldown(getUniqueId(sender), category);
  }

  /**
   * Same as {@link Cooldown#containsOrAdd(Audience, String, int)} except for
   * {@link Cooldowns#TRANSIENT_CATEGORY}
   *
   * @param sender The sender to check
   * @param ticks  The duration of the cooldown
   * @return Same as {@link Cooldown#containsOrAdd(Audience, String, int)}
   */
  public static boolean containsOrAdd(Audience sender, @Nonnegative int ticks) {
    return containsOrAdd(sender, TRANSIENT_CATEGORY, ticks);
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
  public static boolean containsOrAdd(
      Audience sender,
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
   * Adds the given sender to the {@link Cooldowns#TRANSIENT_CATEGORY} cooldown for the given ticks
   *
   * @param sender      The sender to add
   * @param timeInTicks The time to add them for
   */
  public static void add(@NotNull Audience sender, @Nonnegative int timeInTicks) {
    add(sender, TRANSIENT_CATEGORY, timeInTicks);
  }

  /**
   * Adds the given sender into the {@link Cooldowns#TRANSIENT_CATEGORY} category for forever
   *
   * @param sender The sender to add
   */
  public static void add(@NotNull Audience sender) {
    add(sender, TRANSIENT_CATEGORY, NO_END_COOLDOWN);
  }

  /**
   * Adds the given sender into the given category for forever
   *
   * @param sender   The sender to add
   * @param category The category to add them to
   */
  public static void add(@NotNull Audience sender, @NotNull String category) {
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

    var cds = Cooldowns.cooldowns();

    if (cds.onCooldown(id, category)) {
      return;
    }

    cds.cooldown(id, category, timeInTicks);
  }

  /**
   * Removes the given sender from any cooldown
   *
   * @param sender The sender to remove
   */
  public static void remove(@NotNull Audience sender) {
    remove(sender, TRANSIENT_CATEGORY);
  }

  /**
   * Removes the given sender from the given cooldown category
   *
   * @param sender   The sender to remove
   * @param category The category to remove the sender from
   */
  public static void remove(@NotNull Audience sender, @NotNull String category) {
    UUID uuid = getUniqueId(sender);
    Cooldowns.cooldowns().remove(uuid, category);
  }

  public static void testAndThrow(Audience audience, String category, long ticks)
      throws CommandSyntaxException
  {
    var id = getUniqueId(audience);
    Cooldowns cooldowns = Cooldowns.cooldowns();
    Duration remaining = cooldowns.getRemainingTime(id, category);

    if (remaining == null) {
      cooldowns.cooldown(id, Tick.of(ticks));
      return;
    }

    if (remaining.getSeconds() == -1) {
      throw Exceptions.format("You can only do this once");
    } else {
      throw Exceptions.format("You can do this again in {0, time}", remaining);
    }
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

    throw new IllegalArgumentException(
        "Audience " + audience + " is not a player, CommandSource or a User"
    );
  }

  private static long toMillis(long ticks) {
    if (ticks == NO_END_COOLDOWN) {
      return NO_END_COOLDOWN;
    }

    return Time.ticksToMillis(ticks);
  }
}