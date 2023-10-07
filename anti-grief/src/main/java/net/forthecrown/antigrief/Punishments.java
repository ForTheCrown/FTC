package net.forthecrown.antigrief;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.forthecrown.Loggers;
import net.forthecrown.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.PeriodFormat;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageRenderer;
import net.forthecrown.text.format.FormatBuilder;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

/**
 * A general utility class for easisly accessing methods for the punishment and pardoning of users
 */
public final class Punishments {
  private Punishments() {}

  private static final Logger LOGGER = Loggers.getLogger();

  static final PunishmentManager inst = new PunishmentManager();

  public static PunishmentManager get() {
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
      sender.sendMessage(text("You are muted!", NamedTextColor.RED));
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
  public static @Nullable PunishEntry entry(Audience sender) {
    LOGGER.debug("sender={}", sender);

    User player = Audiences.getUser(sender);

    if (player == null) {
      return null;
    }

    return inst.getEntry(player.getUniqueId());
  }

  /**
   * Punishes a user and handles all the formalities of doing so
   *
   * @param target   The target of the punishment
   * @param source   The source doing the punishing
   * @param reason   The reason of the punishment, can be null
   * @param duration The length of the punishment, {@link Punishment#INDEFINITE_EXPIRY} for eternal
   *                 punishment
   * @param type     The type of the punishment
   * @param extra    Any extra data for the punishment, only used to jail the user, the extra is the
   *                 jail cell they're in
   */
  public static void handlePunish(
      User target,
      CommandSource source,
      @Nullable String reason,
      @Nullable Duration duration,
      PunishType type,
      @Nullable String extra
  ) {
    Instant now = Instant.now();

    Instant expires = duration == null
        ? null
        : now.plus(duration);

    Punishment punishment = new Punishment(
        source.textName(),
        reason == null || reason.isEmpty() ? type.defaultReason() : reason,
        extra, type,
        now,
        expires
    );

    PunishEntry entry = entry(target);
    assert entry != null;

    announce(source, target, type, duration, reason);

    var lengthString = duration == null
        ? "Eternal"
        : PeriodFormat.of(duration).toString();

    LOGGER.info("{} punished {} with {}, reason: {}, length: {}",
        source.textName(), target.getName(),
        type.name().toLowerCase(),
        reason,
        lengthString
    );

    entry.punish(punishment);
  }

  /**
   * Announces the punishment
   *
   * @param source   The source giving out the punishment
   * @param target   The target of the punishment
   * @param type     The punishment's type
   * @param duration The punishment's length
   * @param reason   The reason
   */
  public static void announce(
      CommandSource source,
      User target,
      PunishType type,
      Duration duration,
      String reason
  ) {
    _announce(source, viewer -> {
      TextComponent.Builder builder = text()
          .append(text(type.nameEndingED() + " "))
          .color(NamedTextColor.YELLOW)
          .append(target.displayName(viewer).color(NamedTextColor.GOLD));

      if (duration != null) {
        builder
            .append(text(" for "))
            .append(PeriodFormat.of(duration).asComponent().color(NamedTextColor.GOLD));
      }

      if (!Strings.isNullOrEmpty(reason)) {
        builder.append(text(", reason: "))
            .append(text(reason).color(NamedTextColor.GOLD));
      }

      return builder.build();
    });
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
        viewer -> {
          return FormatBuilder.builder()
              .setViewer(viewer)
              .setFormat("Un{0} {1, user}", NamedTextColor.YELLOW)
              .setArguments(type.nameEndingED().toLowerCase(), target)
              .asComponent();
        }
    );
  }

  public static void announceExpiry(User user, PunishType type, Punishment punishment) {
    _announce(
        null,
        viewer -> {
          var builder = text();
          builder.append(user.displayName(viewer).color(NamedTextColor.YELLOW))
              .color(NamedTextColor.GRAY)
              .append(text("'s "))
              .append(text(type.nameEndingED(), NamedTextColor.GOLD))
              .append(text(" has expired"));

          if (punishment != null && !Strings.isNullOrEmpty(punishment.getReason())) {
            builder
                .append(text(", reason: '"))
                .append(text(punishment.getReason(), NamedTextColor.YELLOW))
                .append(text("'"));
          } else {
            builder.append(text("."));
          }

          return builder.build();
        }
    );
  }

  private static void _announce(CommandSource source, ViewerAwareMessage text) {
    // If punishments should be announced to all, then announce them
    // to all, otherwise send them to staff chat only
    if (!StaffChat.isVanished(source) && config().isAnnouncePunishments()) {
      source.sendMessage(text);

      ChannelledMessage message = ChannelledMessage.create(viewer -> {
        return FormatBuilder.builder()
            .setFormat("{0, user}: {1}")
            .setArguments(source, text)
            .setViewer(viewer)
            .asComponent();
      });

      message.setRenderer(MessageRenderer.FTC_PREFIX).setBroadcast().send();
      return;
    }

    // Tell staff chat
    var message = StaffChat.newMessage().setMessage(text).setLogged(true);

    if (source != null) {
      message.setSource(source);
    } else {
      message.setSource(Grenadier.createSource(Bukkit.getConsoleSender()));
    }

    message.send();
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

  private static AntiGriefConfig config() {
    return JavaPlugin.getPlugin(AntiGriefPlugin.class).getPluginConfig();
  }
}