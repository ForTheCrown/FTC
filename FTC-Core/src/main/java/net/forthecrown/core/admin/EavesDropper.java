package net.forthecrown.core.admin;

import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.DirectMessage;
import net.forthecrown.user.actions.MarriageMessage;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class EavesDropper {

    public static Component PREFIX = Component.text("[")
            .color(NamedTextColor.GRAY)
            .append(Component.text("ED").color(NamedTextColor.DARK_GRAY))
            .append(Component.text("] "));

    public static void send(Component text, Permission permission, @Nullable Predicate<CrownUser> toSkip, boolean log){
        Component formatted = Component.text()
                .append(PREFIX)
                .append(text)
                .build();

        UserManager.getOnlineUsers().forEach(u -> {
            if(toSkip != null && toSkip.test(u)) return;
            if(!u.isEavesDropping() || !u.hasPermission(permission)) return;

            u.sendMessage(formatted);
        });
        if(log) Bukkit.getConsoleSender().sendMessage(formatted);
    }

    public static void reportMuted(Component text, Player player, MuteStatus status){
        CrownUser user = UserManager.getUser(player);

        send(
                Component.text()
                        .append(
                                Component.text()
                                        .color(NamedTextColor.YELLOW)
                                        .append(Component.text(status.edPrefix))
                                        .append(user.displayName())
                                        .append(Component.text(" > ").decorate(TextDecoration.BOLD))
                                        .build()
                        )
                        .append(text)
                        .build(),
                Permissions.EAVESDROP_MUTED,
                u -> u.getName().equalsIgnoreCase(player.getName()),
                true
        );
    }

    public static void reportDM(DirectMessage message){
        send(Component.text()
                .append(Component.text(message.getMuteStatus().edPrefix))
                .append(DirectMessage.getHeader(
                        message.senderDisplayName(),
                        message.receiverDisplayName(),
                        NamedTextColor.GRAY
                ))
                .append(Component.text(" "))
                .append(message.getFormattedText())
                .build(),
                Permissions.EAVESDROP_DM,
                u -> u.getName().equalsIgnoreCase(message.getTarget().textName()) || u.getName().equalsIgnoreCase(message.getSender().textName()),
                false
        );
    }

    public static void reportMarriageDM(MarriageMessage message){
        send(
                Component.text()
                        .append(Component.text(message.getMuteStatus().edPrefix))
                        .append(MarriageMessage.PREFIX.color(NamedTextColor.WHITE))
                        .append(message.getSender().nickDisplayName())
                        .append(Component.text(" -> "))
                        .append(message.getTarget().nickDisplayName())
                        .append(MarriageMessage.POINTER)
                        .append(message.getFormatted())
                        .build(),

                Permissions.EAVESDROP_MARRIAGE,
                u -> {
                    if(u.getName().contains(message.getSender().getName())) return true;
                    return u.getName().contains(message.getTarget().getName());
                },
                false
        );
    }

    public static void reportSignPlacement(Player player, Location location, Component... lines){
        if(lines.length < 4) throw new IllegalStateException("Not enough sign lines");
        if(player.hasPermission(Permissions.EAVESDROP_ADMIN)) return;
        if(isSignEmpty(lines)) return;

        CrownUser user = UserManager.getUser(player);
        Component border = Component.text("------------------").decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.GRAY);

        send(
                Component.text()
                        .append(Component.text()
                                .append(user.displayName())
                                .append(Component.text(" placed a sign at "))
                                .append(FtcFormatter.clickableLocationMessage(location, true))
                                .color(NamedTextColor.GRAY)
                        )

                        .append(Component.newline())
                        .append(lines[0])

                        .append(Component.newline())
                        .append(lines[1])

                        .append(Component.newline())
                        .append(lines[2])

                        .append(Component.newline())
                        .append(lines[3])

                        .append(Component.newline())
                        .append(border)
                        .build(),

                Permissions.EAVESDROP_SIGNS,
                u -> u.getName().equalsIgnoreCase(player.getName()),
                false
        );
    }

    public static boolean isSignEmpty(Component[] lines){
        byte emptyLines = 0;

        for (Component c: lines){
            emptyLines += ChatUtils.plainText(c).isBlank() ? 1 : 0;
        }

        return emptyLines >= 4;
    }

    public static void bannedWordChat(Component rendered) {
        Component finalMessage = Component.text()
                .color(NamedTextColor.GRAY)
                .content("(muted) ")
                .append(rendered)
                .build();

        send(
                finalMessage,
                Permissions.EAVESDROP_MUTED,
                user -> true,
                true
        );
    }
}