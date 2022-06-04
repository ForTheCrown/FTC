package net.forthecrown.core.admin;

import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
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

/**
 * A general utility class for easisly accessing
 * methods for the punishment and pardoning of users
 */
public final class Punishments {
    private Punishments() {}

    public static final long INDEFINITE_EXPIRY = -1;
    public static final Var<Boolean> ANNOUNCE_PUNISHMENTS_TO_ALL = Var.def("announcePunishments", VarTypes.BOOL, false);

    static Punisher punisher;

    /**
     * Checks if the given UUID is softmuted
     * @param uuid The UUID to check
     * @return True, if softmuted, false otherwise
     */
    public static boolean isSoftMuted(UUID uuid) {
        PunishEntry entry = punisher.getNullable(uuid);
        if(entry == null) return false;

        return entry.isPunished(PunishType.SOFT_MUTE);
    }

    /**
     * Checks if the user is muted, will tell the user 'You are muted!'
     * if the result is {@link MuteStatus#HARD}
     * @param sender The sender to check, can be player or user
     * @return The sender's mute status
     */
    public static MuteStatus checkMute(CommandSender sender) {
        MuteStatus status = muteStatus(sender);

        if(status == MuteStatus.HARD) {
            sender.sendMessage(
                    Component.text("You are muted!", NamedTextColor.RED)
            );
        }

        return status;
    }

    /**
     * Checks given sender's mute status
     * @param sender The sender to check
     * @return The sender's mute status
     */
    public static MuteStatus muteStatus(CommandSender sender) {
        PunishEntry entry = entry(sender);
        if(entry == null) return MuteStatus.SOFT;

        Punishment p = entry.getCurrent(PunishType.SOFT_MUTE);
        if(p != null) return MuteStatus.SOFT;

        p = entry.getCurrent(PunishType.MUTE);
        return p == null ? MuteStatus.NONE : MuteStatus.HARD;
    }

    /**
     * Gets a current punishment via type for the given sender
     * @param sender The sender
     * @param type The type of punishment to get the entry of
     * @return The currently effective punishment, null, if the sender is not
     *         punished with the given punishment type
     */
    public static Punishment current(CommandSender sender, PunishType type) {
        PunishEntry entry = entry(sender);
        if(entry == null) return null;

        return entry.getCurrent(type);
    }

    /**
     * Gets the punishment entry for the given sender
     * @param sender The sender to get the entry of
     * @return The sender's entry, null, if the sender is not a player or a user
     */
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

    /**
     * Delegate method for {@link BannedWords#checkAndWarn(CommandSender, String)}
     * @param sender The sender of the input
     * @param input The input
     * @return True, if it contains banned words, false otherwise
     */
    public static boolean checkBannedWords(CommandSender sender, Component input) {
        return BannedWords.checkAndWarn(sender, input);
    }

    /**
     * Punishes a user and handles all the formalities of doing so
     * @param target The target of the punishment
     * @param source The source doing the punishing
     * @param reason The reason of the punishment, can be null
     * @param length The length of the punishment,
     *               {@link Punishments#INDEFINITE_EXPIRY} for eternal punishment
     * @param type The type of the punishment
     * @param extra Any extra data for the punishment, only used to
     *              jail the user, the extra is the jail cell they're in
     *
     */
    public static void handlePunish(CrownUser target, CommandSource source, @Nullable String reason, long length, PunishType type, @Nullable String extra) {
        Punishment punishment = new Punishment(
                source.textName(),
                reason == null || reason.isEmpty() ? type.defaultReason() : reason,
                extra, type,
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

        // Log punishment on Staff log
        FtcDiscord.staffLog("Punishments", "**{} {} {}**, reason: ``{}``, length: **{}**",
                source.textName(),
                type.nameEndingED(),
                target.getNickOrName(),
                reason == null ? "None" : reason,
                length == INDEFINITE_EXPIRY ? "Eternal" : new TimePrinter(length).printString()
        );
    }

    /**
     * lol
     * @param cell The cell to place the user in
     * @param user The user to place in jail
     */
    public static void placeInGayBabyJail(JailCell cell, CrownUser user) {
        Location l = FtcUtils.vecToLocation(cell.getWorld(), cell.getPos());
        user.getPlayer().teleport(l);

        punisher.setJailed(user.getUniqueId(), cell);
    }

    /**
     * hehe
     * @param user The user to remove from jail
     */
    public static void removeFromGayBabyJail(CrownUser user) {
        if(user.isOnline()) {
            user.getPlayer().teleport(FtcUtils.findHazelLocation());
        }

        punisher.removeJailed(user.getUniqueId());
    }

    /**
     * Announces the punishment
     * @param source The source giving out the punishment
     * @param target The target of the punishment
     * @param type The punishment's type
     * @param length The punishment's length
     * @param reason The reason
     */
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

    /**
     * Announces the pardoning of a user
     * @param source The source to pardon
     * @param target The target
     * @param type The type they were pardoned from
     */
    public static void announcePardon(CommandSource source, CrownUser target, PunishType type) {
        _announce(
                source, target,
                Component.text("Un" + type.nameEndingED() + " ")
                        .color(NamedTextColor.YELLOW)
                        .append(target.nickDisplayName().color(NamedTextColor.YELLOW))
        );

        FtcDiscord.staffLog("Punishments", "{} Un{} {}", source.textName(), type.nameEndingED().toLowerCase(), target.getNickOrName());
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

    /**
     * Checks if the given user has any staff notes
     * @param user The user to check
     * @return True, if they have notes, false otherwise
     */
    public static boolean hasNotes(CrownUser user) {
        PunishEntry entry = punisher.getNullable(user.getUniqueId());
        return entry != null && !entry.notes().isEmpty();
    }

    /**
     * Checks if the source can punish the given user
     * @param source The source attempting to punish
     * @param user The user to punish
     * @return True, if the source is either OP or has {@link Permissions#ADMIN} permissions OR
     *         the target does not have those permissions
     */
    public static boolean canPunish(CommandSource source, CrownUser user) {
        if(source.isOp() || source.hasPermission(Permissions.ADMIN)) return true;
        return !user.isOp() && !user.hasPermission(Permissions.ADMIN);
    }
}