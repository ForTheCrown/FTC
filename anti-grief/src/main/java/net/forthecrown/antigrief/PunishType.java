package net.forthecrown.antigrief;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.FtcServer;
import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@RequiredArgsConstructor
public enum PunishType {

  /**
   * A mute where the muted individual will think they are not muted because the messages they send
   * will only be visible to themselves
   */
  SOFT_MUTE(GriefPermissions.PUNISH_SOFTMUTE),

  /**
   * Full mute where the person is not able to speak, and they will be told they are muted
   */
  MUTE(GriefPermissions.PUNISH_MUTE),

  /**
   * You get booted from the server lol
   */
  KICK(GriefPermissions.PUNISH_KICK) {
    @Override
    public void onPunishmentStart(User user, PunishEntry entry, Punishment punishment) {
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
  JAIL(GriefPermissions.PUNISH_JAIL) {
    @Override
    public void onPunishmentEnd(User user, PunishEntry entry, Punishment punishment) {
      // If user is online, move them out of jail
      if (user.isOnline()) {
        Location location = FtcServer.server().getServerSpawn();
        user.getPlayer().teleport(location);
      }

      // Remove the user from the jail
      var punisher = Punishments.get();
      punisher.removeJailed(user.getUniqueId());
    }

    @Override
    public void onPunishmentStart(User user, PunishEntry entry, Punishment punishment) {
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

      var punisher = Punishments.get();
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
  BAN(GriefPermissions.PUNISH_BAN) {
    @Override
    public void onPunishmentEnd(User user, PunishEntry entry, Punishment punishment) {
      BanList<PlayerProfile> list = Bukkit.getBanList(Type.PROFILE);
      list.pardon(user.getProfile());
    }

    @Override
    public void onPunishmentStart(User user, PunishEntry entry, Punishment punishment) {
      PunishType.placeInBanList(user, punishment, Type.PROFILE, user.getProfile());
    }
  },

  /**
   * A punishment where a specific IP address is forbidden from joining the server
   */
  IP_BAN(GriefPermissions.PUNISH_BANIP) {
    @Override
    public void onPunishmentEnd(User user, PunishEntry entry, Punishment punishment) {
      // When the punishment ends, remove them from
      // the ip ban list
      BanList<InetAddress> list = Bukkit.getBanList(BanList.Type.IP);
      list.pardon(user.getIp());
    }

    @Override
    public void onPunishmentStart(User user, PunishEntry entry, Punishment punishment) {
      InetAddress addr;

      try {
        addr = InetAddress.getByName(user.getIp());
      } catch (UnknownHostException exc) {
        Loggers.getLogger().error("Cannot IP of user '{}'", user, exc);
        return;
      }

      PunishType.placeInBanList(user, punishment, Type.IP, addr);
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
   * @param user User for whom the punishment ended
   * @param entry User's punishment entry
   * @param punishment Punishment data
   */
  public void onPunishmentEnd(User user, PunishEntry entry, Punishment punishment) {

  }

  /**
   * Callback function for when this type of {@link Punishment} is given
   *
   * @param user       The user being punished
   * @param entry      The entry the punishment will be stroed under
   * @param punishment The punishment entry
   */
  public void onPunishmentStart(User user, PunishEntry entry, Punishment punishment) {

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
   * {@link #IP_BAN} which will return a default ban reason configured in the plugin's config
   *
   * @return The punishment's default reason.
   */
  public String defaultReason() {
    AntiGriefPlugin plugin = JavaPlugin.getPlugin(AntiGriefPlugin.class);
    return plugin.getPluginConfig().getDefaultReason(this);
  }



  private static <T> void placeInBanList(User user, Punishment punishment, Type type, T key) {
    Logger logger = Loggers.getLogger();

    // Add IP ban list entry, we're not going
    // manage bans ourselves after all, that's dumb
    BanList<T> list = Bukkit.getBanList(type);
    BanEntry<T> banEntry = list.addBan(
        key,
        punishment.getReason(),

        punishment.getExpires() == null
            ? null
            : Date.from((punishment.getExpires())),

        punishment.getSource()
    );

    if (banEntry == null) {
      logger.error("Failed to create entry for user '{}' in {} ban list", user, type.name());
      return;
    }

    logger.debug(
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