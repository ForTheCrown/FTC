package net.forthecrown.core.admin;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.BannedWords;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Locations;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public final class Punishments {
    private Punishments() {}

    public static final long INDEFINITE_EXPIRY = -1;
    public static final Var<Boolean> ANNOUNCE_PUNISHMENTS_TO_ALL = Var.def("announcePunishments", VarTypes.BOOL, false);

    static Punisher punisher;

    public static boolean isSoftMuted(UUID uuid) {
        PunishEntry entry = punisher.getEntry(uuid);
        return entry.isPunished(PunishType.SOFT_MUTE);
    }

    public static MuteStatus checkMute(CommandSender sender) {
        MuteStatus status = muteStatus(sender);

        if(status == MuteStatus.HARD) {
            sender.sendMessage(
                    Component.text("You are muted!", NamedTextColor.RED)
            );
        }

        return status;
    }

    public static MuteStatus muteStatus(CommandSender sender) {
        Punishment p = current(sender, PunishType.SOFT_MUTE);
        if(p != null) return MuteStatus.SOFT;

        p = current(sender, PunishType.MUTE);
        return p == null ? MuteStatus.NONE : MuteStatus.HARD;
    }

    public static Punishment current(CommandSender sender, PunishType type) {
        PunishEntry entry = entry(sender);
        if(entry == null) return null;

        return entry.getCurrent(type);
    }

    public static PunishEntry entry(CommandSender sender) {
        UUID uuid = null;

        if(sender instanceof Player player) {
            uuid = player.getUniqueId();
        } else if(sender instanceof CrownUser user) {
            uuid = user.getUniqueId();
        }

        if(uuid == null) return null;

        return punisher.getEntry(uuid);
    }

    public static boolean checkBannedWords(CommandSender sender, String input) {
        return BannedWords.checkAndWarn(sender, input);
    }

    public static boolean checkBannedWords(CommandSender sender, Component input) {
        return BannedWords.checkAndWarn(sender, input);
    }

    public static Punishment handlePunish(CrownUser target, CommandSource source, @Nullable String reason, long length, PunishType type, @Nullable String extra) {
        Punishment punishment = new Punishment(
                source.textName(),
                reason, extra, type,
                System.currentTimeMillis(),
                length == INDEFINITE_EXPIRY ? INDEFINITE_EXPIRY : System.currentTimeMillis() + length
        );

        PunishEntry entry = entry(target);

        type.onPunishmentStart(target, entry, punisher, punishment);
        entry.punish(punishment);

        announce(source, target, type, length, reason);

        Crown.logger().info("{} punished {} with {}, reason: {}, length: {}",
                source.textName(), target.getName(),
                type.name().toLowerCase(),
                reason,
                length == INDEFINITE_EXPIRY ? "Eternal" : new TimePrinter(length).printString()
        );

        return punishment;
    }

    public static void placeInGayBabyJail(JailCell cell, CrownUser user) {
        Location l = Locations.of(cell.getWorld(), cell.getPos());
        user.getPlayer().teleport(l);

        punisher.setJailed(user.getUniqueId(), cell);
    }

    public static void removeFromGayBabyJail(CrownUser user) {
        if(user.isOnline()) {
            user.getPlayer().teleport(FtcUtils.findHazelLocation());
        }

        punisher.removeJailed(user.getUniqueId());
    }

    public static void announce(CommandSource source, CrownUser target, PunishType type, long length, String reason) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text(type.nameEndingED() + " "))
                .color(NamedTextColor.YELLOW)
                .append(target.nickDisplayName().color(NamedTextColor.GOLD));

        if(length != INDEFINITE_EXPIRY) {
            builder.append(Component.text(" for "))
                    .append(new TimePrinter(length).print().color(NamedTextColor.GOLD));
        }

        if(!FtcUtils.isNullOrBlank(reason)) {
            builder.append(Component.text(", reason: "))
                    .append(Component.text(reason).color(NamedTextColor.GOLD));
        }

        _announce(source, target, builder.build());
    }

    public static void announcePardon(CommandSource source, CrownUser target, PunishType type) {
        _announce(
                source, target,
                Component.text("Un" + type.nameEndingED() + " ")
                        .color(NamedTextColor.YELLOW)
                        .append(target.nickDisplayName().color(NamedTextColor.YELLOW))
        );
    }

    private static void _announce(CommandSource source, CrownUser target, Component text) {
        if(!StaffChat.isVanished(source) && ANNOUNCE_PUNISHMENTS_TO_ALL.get()) {
            source.sendMessage(text);

            Component formatted = StaffChat.format(
                    Component.text()
                            .append(FtcFormatter.sourceDisplayName(source).color(NamedTextColor.GRAY))
                            .append(Component.text(": ").style(Style.style(NamedTextColor.DARK_GRAY)))
                            .append(text)
                            .build()
            );

            for (CrownUser u: UserManager.getOnlineUsers()) {
                if(u.getName().equals(source.textName())) continue;
                if(u.getUniqueId().equals(target.getUniqueId())) continue;

                u.sendMessage(formatted);
            }

            return;
        }

        StaffChat.sendCommand(source, text);
    }
}