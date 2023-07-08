package net.forthecrown.core.commands.tpa;

import static net.forthecrown.text.Messages.crossButton;
import static net.forthecrown.text.Messages.tickButton;
import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import java.time.Duration;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public interface TpMessages {

  /**
   * Message used by {@link UserTeleport} to tell users that the delayed teleport was cancelled,
   * most likely because they moved
   */
  TextComponent TELEPORT_CANCELLED = text("Teleport cancelled!", NamedTextColor.GRAY);

  /**
   * Message shown to a user when the {@link UserTeleport#getDestination()} supplier throws an
   * error
   */
  TextComponent TELEPORT_ERROR = text("Cannot teleport, error getting destination",
      NamedTextColor.GRAY
  );

  /**
   * Message stating the viewer is already teleporting
   */
  TextComponent ALREADY_TELEPORTING = text("You are already teleporting!", NamedTextColor.GRAY);

  /**
   * Message stating the viewer denied all incoming TPA requests
   */
  TextComponent TPA_DENIED_ALL = text("Denied all TPA requests", NamedTextColor.YELLOW);

  /**
   * Message format for {@link #tpaTargetMessage(String, User)} for a <code>/tpahere</code> command
   */
  String TPA_FORMAT_HERE = "&e{0, user}&r has requested that you teleport to them. &e{1} &7{2}";

  /**
   * Message format for {@link #tpaTargetMessage(String, User)} for a <code>/tpa</code> command
   */
  String TPA_FORMAT_NORMAL = "&e{0, user}&r has requested to teleport to you. &e{1} &7{2}";

  /**
   * Creates a message that tells the viewer that they will teleport in a given amount of time
   *
   * @param delay The teleportation delay, in milliseconds
   * @param type  Teleportation type
   * @return The formatted message
   */
  static Component teleportStart(Duration delay, UserTeleport.Type type) {
    return format("{0} in &e{1, time}&r\nDon't move!",
        NamedTextColor.GRAY,
        type.getAction(), delay
    );
  }

  /**
   * Message that tells the viewer they are teleporting, or performing
   * {@link UserTeleport.Type#getAction()}
   *
   * @param type The teleportation type
   * @return The formatted type
   */
  static Component teleportComplete(UserTeleport.Type type) {
    return format("{0}...", NamedTextColor.GRAY, type.getAction());
  }

  /**
   * Creates a tpa message that's sent to the target of the tpa request. This method supplies, 3
   * arguments to the given <code>format</code> parameter, they are:
   * <pre>
   * 0: The sender
   * 1: The TPA accept button
   * 2: the TPA deny button
   * </pre>
   *
   * @param format The message format to use, should be one of {@link #TPA_FORMAT_NORMAL} or
   *               {@link #TPA_FORMAT_HERE}
   * @param sender The sender of the message
   * @return The formatted message
   */
  static Component tpaTargetMessage(String format, User sender) {
    return format(format,
        NamedTextColor.GOLD,
        sender,

        tpaAcceptButton(sender),
        tpaDenyButton(sender)
    );
  }

  /**
   * Creates a tpa cancel button
   *
   * @param target The target of the tpa request
   * @return The formatted button component
   */
  static Component tpaCancelButton(User target) {
    return crossButton("/tpacancel %s", target.getName());
  }

  /**
   * Creates a tpa accept button
   *
   * @param sender The sender of the tpa request
   * @return The formatted button component
   */
  static Component tpaAcceptButton(User sender) {
    return tickButton("/tpaccept %s", sender.getName());
  }

  /**
   * Creates a tpa deny button
   *
   * @param sender The sender of the tpa request
   * @return The formatted button component
   */
  static Component tpaDenyButton(User sender) {
    return crossButton("/tpdeny %s", sender.getName());
  }

  /**
   * Creates a message stating the viewer can teleport again in x amount of time
   *
   * @param nextTpTime The next allowed timestamp the user can teleport at
   * @return The formatted component
   */
  static Component canTeleportIn(long nextTpTime) {
    return format("You can teleport again in &6{0, time}",
        NamedTextColor.GRAY,
        Time.timeUntil(nextTpTime)
    );
  }

}
