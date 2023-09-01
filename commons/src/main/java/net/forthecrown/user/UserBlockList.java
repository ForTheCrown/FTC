package net.forthecrown.user;

import static net.forthecrown.text.Messages.BLOCKED_SENDER;
import static net.forthecrown.text.Messages.BLOCKED_TARGET;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * List of blocked/separated users
 */
public interface UserBlockList extends UserComponent {

  /**
   * Tests if the given user was blocked, is blocking, or was separated from the
   * target user. If they were blocked or separated, this method will send the
   * <code>sender</code> a message informing them that they were
   * <p>
   * Argument 0 on the 2 message format parameters will be the given target's
   * {@link User} object... That was a long way to say argument 0 is the
   * target.
   *
   * @param sender              The user
   * @param target              The target user
   * @param senderIgnoredFormat The format to use if the sender has blocked the
   *                            target
   * @param targetIgnoredFormat The format to use if the target has blocked the
   *                            sender
   * @return True, if either of the 2 users has blocked the other or have been
   * separated, false otherwise
   * @see #testBlockedMessage(User, User, String, String)
   */
  static boolean testBlocked(
      User sender,
      User target,
      String senderIgnoredFormat,
      String targetIgnoredFormat
  ) {
    var optional = testBlockedMessage(
        sender, target,
        senderIgnoredFormat, targetIgnoredFormat
    );

    optional.ifPresent(format -> {
      sender.sendMessage(Text.format(format, NamedTextColor.GRAY, target));
    });

    return optional.isPresent();
  }

  /**
   * Tests if the given user was blocked, is blocking, or was separated from the
   * target user. If they were blocked or separated, this method will throw a
   * command syntax exception with the given formats.
   * <p>
   * Argument 0 on the 2 message format parameters will be the given target's
   * {@link User} object... That was a long way to say argument 0 is the
   * target.
   *
   * @param sender              The user
   * @param target              The target user
   * @param senderIgnoredFormat The format to use if the sender has blocked the
   *                            target
   * @param targetIgnoredFormat The format to use if the target has blocked the
   *                            sender
   * @throws CommandSyntaxException If the two users were separated or if either
   *                                had blocked the other
   * @see #testBlockedMessage(User, User, String, String)
   */
  static void testBlockedException(
      User sender,
      User target,
      String senderIgnoredFormat,
      String targetIgnoredFormat
  ) throws CommandSyntaxException {
    var optional = testBlockedMessage(
        sender, target,
        senderIgnoredFormat, targetIgnoredFormat
    );

    if (optional.isEmpty()) {
      return;
    }

    throw Exceptions.format(optional.get(), target);
  }

  /**
   * Tests if the given user was blocked, is blocking, or was separated from the
   * target user. If they were blocked or separated, this method will return the
   * corresponding message from the 2 ignore formats given. If the user and
   * sender are forcefully separated, the result is
   * {@link Messages#SEPARATED_FORMAT}. If the users aren't blocked or separated
   * at all, an empty optional is returned
   * <p>
   * Argument 0 on the 2 message format parameters will be the given target's
   * {@link User} object... That was a long way to say argument 0 is the
   * target.
   *
   * @param sender              The user
   * @param target              The target user
   * @param senderIgnoredFormat The format to use if the sender has blocked the
   *                            target
   * @param targetIgnoredFormat The format to use if the target has blocked the
   *                            sender
   * @return Corresponding ignore message, empty, if not blocked or separated in
   * any way
   */
  static Optional<String> testBlockedMessage(
      User sender,
      User target,
      String senderIgnoredFormat,
      String targetIgnoredFormat
  ) {
    var state = getIgnoreState(sender, target);
    return switch (state) {
      case NONE -> Optional.empty();
      case SEPARATED -> Optional.of(Messages.SEPARATED_FORMAT);
      case SENDER_IGNORED_TARGET -> Optional.of(senderIgnoredFormat);
      case TARGET_IGNORED_SENDER -> Optional.of(targetIgnoredFormat);
    };
  }

  /**
   * Tests if either of the 2 uses have blocked each other or have been
   * separated
   *
   * @param sender The first user
   * @param target The second user
   * @return True, if either has blocked the other or they've been separated,
   * false otherwise
   */
  static boolean areBlocked(User sender, User target) {
    return getIgnoreState(sender, target) != IgnoredState.NONE;
  }

  static IgnoredState getIgnoreState(User sender, User target) {
    if (target.equals(sender)) {
      return IgnoredState.NONE;
    }

    UserBlockList userInter = sender.getComponent(UserBlockList.class);
    UserBlockList targetInter = target.getComponent(UserBlockList.class);

    IgnoreResult sender2Target = userInter.testIgnored(target);
    IgnoreResult target2Sender = targetInter.testIgnored(sender);

    if (sender2Target == IgnoreResult.SEPARATED || target2Sender == IgnoreResult.SEPARATED) {
      return IgnoredState.SEPARATED;
    }

    if (sender2Target == IgnoreResult.BLOCKED) {
      return IgnoredState.SENDER_IGNORED_TARGET;
    }

    if (target2Sender == IgnoreResult.BLOCKED) {
      return IgnoredState.TARGET_IGNORED_SENDER;
    }

    return IgnoredState.NONE;
  }

  static Optional<Component> filterPlayers(
      User sender,
      Collection<User> users,
      @Nullable UserProperty<Boolean> filterTest,
      @Nullable String propertyFailMessage,
      @Nullable Component selfRemovedMessage
  ) {
    if (filterTest != null) {
      Objects.requireNonNull(propertyFailMessage, "propertyFailMessage missing");
    }

    // Only remove self if a self-removal message is set
    if (selfRemovedMessage != null) {
      boolean selfRemoved = users.size() == 1 && users.remove(sender);
      if (selfRemoved) {
        return Optional.of(selfRemovedMessage);
      }
    }

    if (users.size() == 1) {
      var target = users.iterator().next();
      var opt = testBlockedMessage(sender, target, BLOCKED_SENDER, BLOCKED_TARGET);

      if (opt.isPresent()) {
        return opt.map(s -> Text.format(s, sender, target));
      }

      if (filterTest != null && !target.get(filterTest)) {
        return Optional.of(Text.format(propertyFailMessage, target));
      }

      return Optional.empty();
    }

    users.removeIf(user -> {
      return areBlocked(sender, user) || (filterTest != null && !user.get(filterTest));
    });

    if (users.isEmpty()) {
      var text = Grenadier.exceptions().noPlayerFound().componentMessage();
      assert text != null;
      return Optional.of(text);
    }

    return Optional.empty();
  }

  /**
   * User this block list component is bound to
   * @return List owner
   */
  @NotNull User getUser();

  /**
   * Tests if the specified {@code other} user is ignored or separated
   * @param other User to test
   * @return Ignore result
   */
  @NotNull IgnoreResult testIgnored(@NotNull User other);

  /**
   * Tests if the result {@link #testIgnored(User)} is {@link IgnoreResult#BLOCKED} or
   * {@link IgnoreResult#SEPARATED}
   *
   * @param other User to test
   * @return {@code true}, if the specified {@code other} is blocked or separated,
   *         {@code false} otherwise
   */
  default boolean isBlocked(@NotNull User other) {
    return testIgnored(other).isBlocked();
  }

  /**
   * Sets a specified {@code other} user as ignored or separated.
   * <p>
   * <b>Note:</b> The {@code separated} parameter only determines which of the 2 block lists the
   * specified {@code other} user is placed into, it will not modify the other user's block list
   *
   * @param other User to ignore/separate
   * @param separated {@code true}, if the user is being forcefully blocked
   */
  void setIgnored(@NotNull User other, boolean separated);

  /**
   * Removes the specified {@code other} from this user's blocklist
   * @param other Other user
   */
  void removeIgnored(@NotNull User other);

  /**
   * Removes the specified {@code other} from this user's separated users list.
   * <p>
   * This method will not modify the {@code other} user's blocklist, so to fully remove the forced
   * separation between 2 users, this method must be called for the blocklist of both users
   *
   * @param other Other user
   */
  void removeSeparated(@NotNull User other);

  /**
   * Gets an immutable collection of blocked user IDs
   * @return Blocked user ID set
   */
  @NotNull
  Collection<UUID> getBlocked();

  /**
   * Gets an immutable collection of user IDs that this user has been separated from
   * @return Separated user ID set
   */
  @NotNull
  Collection<UUID> getSeparated();

  /**
   * Value returned by {@link #testIgnored(User)}
   */
  enum IgnoreResult {

    /**
     * Users are not separated and have not blocked each other in any capacity
     */
    NOT_IGNORED,

    /**
     * Users are forcefully separated
     */
    SEPARATED,

    /**
     * User has blocked the target character willingly
     */
    BLOCKED;

    public boolean isBlocked() {
      return this != NOT_IGNORED;
    }
  }

  enum IgnoredState {
    TARGET_IGNORED_SENDER,
    SENDER_IGNORED_TARGET,

    SEPARATED,

    NONE
  }
}