package net.forthecrown.core.admin;

import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Date;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

public enum PunishType {
    SOFT_MUTE,
    MUTE,

    KICK,

    JAIL {
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

    BAN {
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
    },

    IP_BAN {
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
    };

    public void onPunishmentEnd(CrownUser user) {}
    public void onPunishmentStart(CrownUser user, PunishEntry entry, Punisher punisher, Punishment punishment) {}

    public String presentableName() {
        return FtcFormatter.normalEnum(this).replaceAll(" ", "");
    }

    public String nameEndingED() {
        return presentableName() + (name().endsWith("E") ? "d" : "ed");
    }
}