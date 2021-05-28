package net.forthecrown.emperor.admin;

import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.data.DirectMessage;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class EavesDropper {
    public static void send(Component text, Permission permission, @Nullable Predicate<CrownUser> toSkip){
        Component formatted = format(text);

        UserManager.getOnlineUsers().forEach(u -> {
            if(toSkip != null && toSkip.test(u)) return;
            if(!u.isEavesDropping() || !u.hasPermission(permission)) return;
            u.sendMessage(formatted);
        });
    }

    public static Component format(Component initial){
        return Component.text()
                .append(Component.text("[EavesDrop] ").color(NamedTextColor.DARK_GRAY))
                .append(initial)
                .build();
    }

    public static void reportMuted(Component text, Player player, MuteStatus status){
        CrownUser user = UserManager.getUser(player);

        send(Component.text()
                .append(
                        Component.text()
                                .color(NamedTextColor.GRAY)
                                .append(Component.text(status.edPrefix))
                                .append(user.displayName())
                                .append(Component.text(" > ").decorate(TextDecoration.BOLD))
                                .build()
                )
                .append(text)
                .build(),
                Permissions.EAVESDROP_MUTED,
                u -> u.getName().equalsIgnoreCase(player.getName())
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
                Permissions.EAVESDROP,
                u -> u.getName().equalsIgnoreCase(message.getReceiver().textName()) || u.getName().equalsIgnoreCase(message.getSender().textName())
        );
    }

    public static void reportSignPlacement(Player player, Location location, Component... lines){
        if(lines.length < 4) throw new IllegalStateException("Not enough sign lines");
        if(player.hasPermission(Permissions.EAVESDROP_ADMIN)) return;
        CrownUser user = UserManager.getUser(player);
        Component border = Component.text("------------------").decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.GRAY);

        send(
                Component.text()
                        .append(Component.text()
                                .append(user.displayName())
                                .append(Component.text(" placed a sign at "))
                                .append(ChatFormatter.clickableLocationMessage(location, true))
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
                u -> u.getName().equalsIgnoreCase(player.getName())
        );
    }
}
