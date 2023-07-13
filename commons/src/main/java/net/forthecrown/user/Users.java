package net.forthecrown.user;

import static net.forthecrown.text.Messages.BLOCKED_SENDER;
import static net.forthecrown.text.Messages.BLOCKED_TARGET;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.UserBlockList.IgnoreResult;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class Users {
  private Users() {}

  private static UserService service;

  public static UserService getService() {
    return Objects.requireNonNull(service,
        "Service not created (Error during initialization or calling too early?)"
    );
  }

  public static void setService(UserService service) {
    if (Users.service != null) {
      throw new IllegalStateException("Tried changing already set UserService");
    }

    Users.service = service;
  }

  public static User get(OfflinePlayer player) {
    return get(player.getUniqueId());
  }

  public static User get(UUID uuid) {
    var lookup = getService().getLookup();
    var entry = lookup.getEntry(uuid);
    return get(entry);
  }

  public static User get(String str) {
    var lookup = getService().getLookup();
    var entry = lookup.query(str);
    return get(entry);
  }

  public static User get(LookupEntry entry) {
    return getService().getUser(entry);
  }

  public static void forEachUser(Consumer<User> consumer) {
    Bukkit.getOnlinePlayers().forEach(player -> {
      User user = get(player);
      consumer.accept(user);
    });
  }

  /* ----------------------- BLOCKED/IGNORE TESTING ----------------------- */

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
  public static boolean testBlocked(
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
  public static void testBlockedException(
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
  public static Optional<String> testBlockedMessage(
      User sender,
      User target,
      String senderIgnoredFormat,
      String targetIgnoredFormat
  ) {
    if (sender.equals(target)) {
      return Optional.empty();
    }

    UserBlockList userInter = sender.getComponent(UserBlockList.class);
    UserBlockList targetInter = target.getComponent(UserBlockList.class);

    IgnoreResult user2Target = userInter.testIgnored(target);
    IgnoreResult target2User = targetInter.testIgnored(sender);

    if (user2Target == IgnoreResult.SEPARATED || target2User == IgnoreResult.SEPARATED) {
      return Optional.of(Messages.SEPARATED_FORMAT);
    }

    if (user2Target == IgnoreResult.BLOCKED) {
      return Optional.of(senderIgnoredFormat);
    } else if (target2User == IgnoreResult.BLOCKED) {
      return Optional.of(targetIgnoredFormat);
    }

    return Optional.empty();
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
  public static boolean areBlocked(User sender, User target) {
    if (sender.equals(target)) {
      return false;
    }

    UserBlockList userInter = sender.getComponent(UserBlockList.class);
    UserBlockList targetInter = target.getComponent(UserBlockList.class);

    return userInter.isBlocked(target) || targetInter.isBlocked(sender);
  }

  public static Optional<Component> filterPlayers(
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
    boolean selfRemoved = users.size() == 1 && users.remove(sender);
    if (selfRemovedMessage != null && selfRemoved) {
      return Optional.of(selfRemovedMessage);
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

  public static Collection<User> getOnline() {
    return service.getOnlineUsers();
  }
}