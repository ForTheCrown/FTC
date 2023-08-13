package net.forthecrown.core.admin;

import java.util.UUID;
import javax.annotation.Nullable;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.bukkit.OfflinePlayer;

/**
 * A general utility class for easisly accessing methods for the punishment and pardoning of users
 */
public final class Punishments {

  private Punishments() {
  }

  /**
   * Constant for determining if a {@link Punishment} lasts forever, or until pardoned by a staff
   * member.
   */
  public static final long INDEFINITE_EXPIRY = -1;

  private static final Logger LOGGER = Loggers.getLogger();

  static final Punisher inst = new Punisher();

  public static Punisher get() {
    return inst;
  }

  /**
   * Checks if the user is muted, will tell the user 'You are muted!' if the result is
   * {@link Mute#HARD}
   *
   * @param sender The sender to check, can be player or user
   * @return The sender's mute status
   */
  public static Mute checkMute(Audience sender) {
    Mute status = muteStatus(sender);

    if (status == Mute.HARD) {
      sender.sendMessage(Messages.YOU_ARE_MUTED);
    }

    return status;
  }

  /**
   * Checks given sender's mute status
   *
   * @param sender The sender to check
   * @return The sender's mute status
   */
  public static Mute muteStatus(Audience sender) {
    PunishEntry entry = entry(sender);

    if (entry == null) {
      return Mute.NONE;
    }

    Punishment p = entry.getCurrent(PunishType.SOFT_MUTE);
    if (p != null) {
      return Mute.SOFT;
    }

    p = entry.getCurrent(PunishType.MUTE);
    return p == null ? Mute.NONE : Mute.HARD;
  }

  /**
   * Gets the punishment entry for the given sender
   *
   * @param sender The sender to get the entry of
   * @return The sender's entry, null, if the sender is not a player or a user
   */
  public static PunishEntry entry(Audience sender) {
    UUID uuid = null;

    if (sender instanceof OfflinePlayer player) {
      uuid = player.getUniqueId();
    } else if (sender instanceof User user) {
      uuid = user.getUniqueId();
    }

    if (uuid == null) {
      return null;
    }

    return inst.getEntry(uuid);
  }

  /**
   * Punishes a user and handles all the formalities of doing so
   *
   * @param target The target of the punishment
   * @param source The source doing the punishing
   * @param reason The reason of the punishment, can be null
   * @param length The length of the punishment, {@link Punishments#INDEFINITE_EXPIRY} for eternal
   *               punishment
   * @param type   The type of the punishment
   * @param extra  Any extra data for the punishment, only used to jail the user, the extra is the
   *               jail cell they're in
   */
  public static void handlePunish(User target, CommandSource source, @Nullable String reason,
                                  long length, PunishType type, @Nullable String extra
  ) {
    Punishment punishment = new Punishment(
        source.textName(),
        reason == null || reason.isEmpty() ? type.defaultReason() : reason,
        extra, type,
        System.currentTimeMillis(),
        length == INDEFINITE_EXPIRY ? INDEFINITE_EXPIRY : System.currentTimeMillis() + length
    );

    PunishEntry entry = entry(target);
    assert entry != null;

    entry.punish(punishment);

    announce(source, target, type, length, reason);

    var lengthString = length == INDEFINITE_EXPIRY
        ? "Eternal"
        : PeriodFormat.of(length).toString();

    LOGGER.info("{} punished {} with {}, reason: {}, length: {}",
        source.textName(), target.getName(),
        type.name().toLowerCase(),
        reason,
        lengthString
    );
  }

  /**
   * Announces the punishment
   *
   * @param source The source giving out the punishment
   * @param target The target of the punishment
   * @param type   The punishment's type
   * @param length The punishment's length
   * @param reason The reason
   */
  public static void announce(CommandSource source, User target, PunishType type, long length,
                              String reason
  ) {
    TextComponent.Builder builder = Component.text()
        .append(Component.text(type.nameEndingED() + " "))
        .color(NamedTextColor.YELLOW)
        .append(target.displayName().color(NamedTextColor.GOLD));

    if (length != INDEFINITE_EXPIRY) {
      builder.append(Component.text(" for "))
          .append(PeriodFormat.of(length).asComponent().color(NamedTextColor.GOLD));
    }

    if (!Util.isNullOrBlank(reason)) {
      builder.append(Component.text(", reason: "))
          .append(Component.text(reason).color(NamedTextColor.GOLD));
    }

    _announce(source, builder.build());
  }

  /**
   * Announces the pardoning of a user
   *
   * @param source The source to pardon
   * @param target The target
   * @param type   The type they were pardoned from
   */
  public static void announcePardon(CommandSource source, User target, PunishType type) {
    _announce(
        source,
        Component.text("Un" + type.nameEndingED() + " ")
            .color(NamedTextColor.YELLOW)
            .append(target.displayName().color(NamedTextColor.YELLOW))
    );

    LOGGER.info(Loggers.STAFF_LOG, "{} Un{} {}",
        source.textName(),
        type.nameEndingED().toLowerCase(),
        target.getNickOrName()
    );
  }

  private static void _announce(CommandSource source, Component text) {
    // If punishments should be announce to all, then announce them
    // to all, otherwise send them to staff chat only
    if (!StaffChat.isVanished(source) && GeneralConfig.announcePunishments) {
      source.sendMessage(text);

      Announcer.get().announce(
          Text.format("[Staff] {0}: {1}", Text.sourceDisplayName(source), text)
      );
      return;
    }

    // Tell staff chat
    StaffChat.newMessage()
        .setSource(source)
        .setMessage(text)
        .send();
  }

  /**
   * Checks if the given user has any staff notes
   *
   * @param user The user to check
   * @return True, if they have notes, false otherwise
   */
  public static boolean hasNotes(User user) {
    PunishEntry entry = inst.getNullable(user.getUniqueId());
    return entry != null && !entry.getNotes().isEmpty();
  }

  /**
   * Checks if the source can punish the given user
   *
   * @param source The source attempting to punish
   * @param user   The user to punish
   * @return True, if the source is either OP or has {@link Permissions#ADMIN} permissions OR the
   * target does not have those permissions
   */
  public static boolean canPunish(CommandSource source, User user) {
    if (source.isOp() || source.hasPermission(Permissions.ADMIN)) {
      return true;
    }

    return !user.getOfflinePlayer().isOp() && !user.hasPermission(Permissions.ADMIN);
  }
}