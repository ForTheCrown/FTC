package net.forthecrown.utils;

import java.util.Objects;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.ForwardingAudience.Single;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class Audiences {
  private Audiences() {}

  /**
   * Gets a player from an {@link Audience}.
   * <p>
   * Function works by calling {@link #unwrap(Audience)} and checking if the returned result is
   * an instance of {@link Player}, if it is, that player is returned, otherwise, null is returned
   *
   * @param audience Audience to get the player of
   * @return Player, derived from the {@code audience}, or {@code null}, if the {@code audience}
   *         was null or a player couldn't be derived from it
   */
  @Contract("null -> null")
  public static @Nullable Player getPlayer(@Nullable Audience audience) {
    if (audience == null) {
      return null;
    }

    audience = unwrap(audience);

    if (audience instanceof Player player) {
      return player;
    }

    return null;
  }

  /**
   * Similar to {@link #getPlayer(Audience)}, however this method doesn't fail in the case the user
   * is offline
   *
   * @param audience Audience to get the user of
   * @return User derived from the {@code audience}, or {@code null}, the audience was null or a
   *         user couldn't be derived from it
   */
  @Contract("null -> null")
  public static @Nullable User getUser(@Nullable Audience audience) {
    while (true) {
      if (audience instanceof User user) {
        return user;
      }

      if (audience instanceof Player player) {
        return Users.get(player);
      }

      if (audience instanceof ForwardingAudience.Single single) {
        audience = single.audience();
        continue;
      }

      return null;
    }
  }

  /**
   * "unwraps" the specified {@code audience}. This is done by checking if the input is an instance
   * of {@link ForwardingAudience.Single} and then recursively calling {@link Single#audience()}
   * until the base audience has been reached
   *
   * @param audience Audience to unwrap
   * @return Unwrapped audience
   */
  public static Audience unwrap(Audience audience) {
    if (audience instanceof ForwardingAudience.Single single) {
      return unwrap(single.audience());
    } else {
      return audience;
    }
  }

  public static boolean equals(Audience first, Audience second) {
    return Objects.equals(unwrap(first), unwrap(second));
  }
}