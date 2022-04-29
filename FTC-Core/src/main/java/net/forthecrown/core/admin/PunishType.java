package net.forthecrown.core.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.permissions.Permission;

import java.util.Date;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

/**
 * Represents a type of punishment a user can receive
 */
@RequiredArgsConstructor
public enum PunishType {
    /**
     * A mute where the muted individual will think they
     * are not muted because the messages they send will
     * only be visible to themselves
     */
    SOFT_MUTE (Permissions.HELPER),

    /**
     * Full mute where the person is not able to speak,
     * and they will be told they are muted
     */
    MUTE (Permissions.HELPER),

    /**
     * You get booted from the server lol
     */
    KICK (Permissions.HELPER),

    /**
     * Punishment where a person is placed inside a
     * cell they cannot leave, yknow, jailed.
     * <p></p>
     * PS, please don't look at the method calls
     * inside this type
     */
    JAIL (Permissions.HELPER) {
        @Override
        public void onPunishmentEnd(CrownUser user) {
            Punishments.removeFromGayBabyJail(user);
        }

        @Override
        public void onPunishmentStart(CrownUser user, PunishEntry entry, Punisher punisher, Punishment punishment) {
            Key k = Keys.parse(punishment.extra());
            JailCell cell = Registries.JAILS.get(k);

            Punishments.placeInGayBabyJail(cell, user);
        }
    },

    /**
     * A punishment where a person is forbidden from joining
     * the server
     */
    BAN (Permissions.POLICE) {
        @Override
        public void onPunishmentEnd(CrownUser user) {
            BanList list = Bukkit.getBanList(BanList.Type.NAME);
            list.pardon(user.getName());
        }

        @Override
        public void onPunishmentStart(CrownUser user, PunishEntry entry, Punisher punisher, Punishment punishment) {
            BanList list = Bukkit.getBanList(BanList.Type.NAME);
            list.addBan(
                    user.getName(),
                    punishment.reason(),
                    punishment.expires() == INDEFINITE_EXPIRY ? null : new Date(punishment.expires()),
                    punishment.source()
            );

            if(user.isOnline()) {
                user.getPlayer().kick(Component.text(punishment.reason()), PlayerKickEvent.Cause.IP_BANNED);
            }
        }

        @Override
        public String defaultReason() {
            return FtcVars.defaultBanReason.get();
        }
    },

    /**
     * A punishment where a specific IP address is forbidden
     * from joining the server
     */
    IP_BAN (Permissions.POLICE) {
        @Override
        public void onPunishmentEnd(CrownUser user) {
            BanList list = Bukkit.getBanList(BanList.Type.IP);
            list.pardon(user.getIp());
        }

        @Override
        public void onPunishmentStart(CrownUser user, PunishEntry entry, Punisher punisher, Punishment punishment) {
            BanList list = Bukkit.getBanList(BanList.Type.IP);
            list.addBan(
                    user.getIp(),
                    punishment.reason(),
                    punishment.expires() == INDEFINITE_EXPIRY ? null : new Date(punishment.expires()),
                    punishment.source()
            );

            if(user.isOnline()) {
                user.getPlayer().kick(Component.text(punishment.reason()), PlayerKickEvent.Cause.IP_BANNED);
            }
        }

        @Override
        public String defaultReason() {
            return FtcVars.defaultBanReason.get();
        }
    };

    @Getter
    private final Permission permission;

    public void onPunishmentEnd(CrownUser user) {}
    public void onPunishmentStart(CrownUser user, PunishEntry entry, Punisher punisher, Punishment punishment) {}

    public String presentableName() {
        return FtcFormatter.normalEnum(this).replaceAll(" ", "");
    }

    public String nameEndingED() {
        String initial = presentableName().replaceAll("Ban", "Bann");
        return initial + (name().endsWith("E") ? "d" : "ed");
    }

    public String defaultReason() {
        return null;
    }
}