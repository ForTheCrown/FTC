package net.forthecrown.core.admin;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.permissions.Permission;

/**
 * Represents a type of punishment a user can receive
 */
@RequiredArgsConstructor
public enum PunishType {
  /**
   * A mute where the muted individual will think they are not muted because the messages they send
   * will only be visible to themselves
   */
  SOFT_MUTE(Permissions.PUNISH_SOFTMUTE),

  /**
   * Full mute where the person is not able to speak, and they will be told they are muted
   */
  MUTE(Permissions.PUNISH_MUTE),

  /**
   * You get booted from the server lol
   */
  KICK(Permissions.PUNISH_KICK) {
    @Override
    public void onPunishmentStart(User user,
                                  PunishEntry entry,
                                  Punisher punisher,
                                  Punishment punishment
    ) {
      if (!user.isOnline()) {
        return;
      }

      user.getPlayer().kick(
          punishment.getReason() == null
              ? null
              : Text.renderString(punishment.getReason()),

          PlayerKickEvent.Cause.KICK_COMMAND
      );
    }
  },

  /**
   * Punishment where a person is placed inside a cell they cannot leave, y'know, jailed.
   */
  JAIL(Permissions.PUNISH_JAIL) {
    @Override
    public void onPunishmentEnd(User user, Punisher punisher) {
      // If user is online, move them out of jail
      if (user.isOnline()) {
        user.getPlayer().teleport(GeneralConfig.getServerSpawn());
      }

      // Remove the user from the jail
      punisher.removeJailed(user.getUniqueId());
    }

    @Override
    public void onPunishmentStart(User user,
                                  PunishEntry entry,
                                  Punisher punisher,
                                  Punishment punishment
    ) {
      // Place user in jail
      String k = punishment.getExtra();
      var cellOptional = Punishments.get().getCells().get(k);

      if (cellOptional.isEmpty()) {
        Loggers.getLogger().warn("Cannot jail {}, unknown jail name: '{}'",
            user.getName(), k
        );
        return;
      }

      JailCell cell = cellOptional.get();

      punisher.setJailed(user.getUniqueId(), cell);

      // Only move user to jail if they're online
      if (user.isOnline()) {
        var pos = cell.getPos();
        Location l = new Location(cell.getWorld(), pos.x(), pos.y(), pos.z());

        user.getPlayer().teleport(l);
      }
    }
  },

  /**
   * A punishment where a person is forbidden from joining the server
   */
  BAN(Permissions.PUNISH_BAN) {
    @Override
    public void onPunishmentEnd(User user, Punisher punisher) {
      BanList list = Bukkit.getBanList(BanList.Type.NAME);
      list.pardon(user.getName());
    }

    @Override
    public void onPunishmentStart(User user,
                                  PunishEntry entry,
                                  Punisher punisher,
                                  Punishment punishment
    ) {
       PunishType.placeInBanList(user, punishment, Type.NAME);
    }

    @Override
    public String defaultReason() {
      return GeneralConfig.defaultBanReason;
    }
  },

  /**
   * A punishment where a specific IP address is forbidden from joining the server
   */
  IP_BAN(Permissions.PUNISH_BANIP) {
    @Override
    public void onPunishmentEnd(User user, Punisher punisher) {
      // When the punishment ends, remove them from
      // the ip ban list
      BanList list = Bukkit.getBanList(BanList.Type.IP);
      list.pardon(user.getIp());
    }

    @Override
    public void onPunishmentStart(User user,
                                  PunishEntry entry,
                                  Punisher punisher,
                                  Punishment punishment
    ) {
      PunishType.placeInBanList(user, punishment, Type.IP);
    }

    @Override
    public String defaultReason() {
      return GeneralConfig.defaultBanReason;
    }
  };

  public static final PunishType[] TYPES = values();

  /**
   * The permission level needed to bestow this punishment upon people
   */
  @Getter
  private final Permission permission;

  /**
   * Callback function for when a {@link Punishment} of this type ends, expires or is pardoned.
   *
   * @param punisher The punishment manager instance
   * @param user     The user for whom the punishment ended
   */
  public void onPunishmentEnd(User user, Punisher punisher) {
  }

  /**
   * Callback function for when this type of {@link Punishment} is given
   *
   * @param user       The user being punished
   * @param entry      The entry the punishment will be stroed under
   * @param punisher   The punishment manager instance
   * @param punishment The punishment entry
   */
  public void onPunishmentStart(User user,
                                PunishEntry entry,
                                Punisher punisher,
                                Punishment punishment
  ) {
  }

  public String presentableName() {
    return Text.prettyEnumName(this).replaceAll(" ", "");
  }

  public String nameEndingED() {
    String initial = presentableName().replaceAll("Ban", "Bann");
    return initial + (name().endsWith("E") ? "d" : "ed");
  }

  /**
   * Gets the default reason for this punishment
   * <p>
   * Most instances of {@link PunishType} return null for this method, except for {@link #BAN} and
   * {@link #IP_BAN} which will return {@link GeneralConfig#defaultBanReason}
   *
   * @return The punishment's default reason.
   */
  public String defaultReason() {
    return null;
  }

  private static void placeInBanList(User user,
                                     Punishment punishment,
                                     Type type
  ) {
    Logger logger = Loggers.getLogger();
    String key = type == Type.IP
        ? user.getIp()
        : user.getUniqueId().toString();

    // Add IP ban list entry, we're not going
    // manage bans ourselves after all, that's dumb
    BanList list = Bukkit.getBanList(type);
    var banEntry = list.addBan(
        key,
        punishment.getReason(),
        punishment.getExpires() == INDEFINITE_EXPIRY
            ? null
            : new Date(punishment.getExpires()),

        punishment.getSource()
    );

    if (banEntry == null) {
      throw new IllegalStateException("Failed to create ban entry for " + user);
    }

    logger.info(
        "Created banlist entry for {}, entry={}",
        user, toString(banEntry)
    );

    banEntry.save();

    // If the player is online, kick them
    if (user.isOnline()) {
      user.getPlayer().kick(
          Component.text(punishment.getReason()),
          type == Type.IP ? Cause.IP_BANNED : Cause.BANNED
      );
    }
  }

  private static String toString(BanEntry entry) {
    return String.format(
        "BanEntry[target=%s, created=%s, reason=%s, expires=%s]",
        entry.getTarget(),
        entry.getCreated(),
        entry.getReason(),
        entry.getExpiration()
    );
  }
}