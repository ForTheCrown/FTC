package net.forthecrown.core.admin;

import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.Permissions;
import net.forthecrown.utils.text.Text;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.forthecrown.core.FtcDiscord.C_STAFF;

/**
 * Class representing the staff chat
 * <p>Exists because I was cleaning up the ChatEvents class lol</p>
 */
public final class StaffChat {
    private StaffChat() {}

    public static final Set<UUID> toggledPlayers = new HashSet<>();

    public static final Component
            PREFIX = Component.text("[Staff] ").color(NamedTextColor.DARK_GRAY),
            VANISH_PREFIX = Component.text("[VANISH] ").color(NamedTextColor.WHITE);

    /**
     * Sends a staff chat message
     * @param sender The message's sender
     * @param message The message
     * @param cmd Whether the message was sent via command (If true, message won't get logged)
     */
    public static void send(@NotNull CommandSource sender, @NotNull Component message, boolean cmd){
        Validate.notNull(sender, "Sender was null");

        Component senderText = Text.sourceDisplayName(sender);
        //Staff chat format component
        TextComponent text = Component.text()
                .append(vanishPrefix(sender))
                .append(senderText.color(NamedTextColor.GRAY))
                .append(Component.text(" > ").style(Style.style(NamedTextColor.DARK_GRAY, TextDecoration.BOLD)))
                .append(message)
                .build();

        FtcDiscord.staffLog(C_STAFF, "{} **>** {}", Text.plain(senderText), Text.LEGACY.serialize(message));
        send(text, !cmd);
    }

    /**
     * Sends a message to the staff chat in a command format,
     * example: '[Staff] source_name: message'
     * @param source The source sending the message
     * @param msg The message to send
     */
    public static void sendCommand(CommandSource source, Component msg) {
        send(
                Component.text()
                        .append(vanishPrefix(source))
                        .append(Text.sourceDisplayName(source).color(NamedTextColor.GRAY))
                        .append(Component.text(": ").style(Style.style(NamedTextColor.DARK_GRAY)))
                        .append(msg)
                        .build()
                ,
                true
        );
    }

    static Component vanishPrefix(CommandSource source) {
        return isVanished(source) ? VANISH_PREFIX : Component.empty();
    }

    public static boolean isVanished(CommandSource source) {
        return source.isPlayer() && Users.get(source.asOrNull(Player.class)).get(Properties.VANISHED);
    }

    /**
     * Sends a message to the staff chat
     * @param text The message to send
     * @param shouldLog True, to log the message in console
     */
    public static void send(Component text, boolean shouldLog) {
        Component message = format(text);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if(!p.hasPermission(Permissions.STAFF_CHAT)) {
                continue;
            }

            p.sendMessage(message);
        }

        if (shouldLog) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

    public static Component format(Component text) {
        return Component.text()
                .append(PREFIX)
                .append(text)
                .build();
    }
}