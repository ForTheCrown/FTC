package net.forthecrown.core.admin;

import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
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

/**
 * Class representing the staff chat
 * <p>Exists because I was cleaning up the ChatEvents class lol</p>
 */
public class StaffChat {
    public static final Set<Player> toggledPlayers = new HashSet<>();
    public static final Set<Player> ignoring = new HashSet<>();
    public static final Component PREFIX = Component.text("[Staff] ").color(NamedTextColor.DARK_GRAY);

    /**
     * Sends a staff chat message
     * @param sender The message's sender
     * @param message The message
     * @param cmd Whether the message was sent via command (If true, message won't get logged)
     */
    public static void send(@NotNull CommandSource sender, @NotNull Component message, boolean cmd){
        Validate.notNull(sender, "Sender was null");

        Component senderText = CrownUtils.sourceDisplayName(sender);
        //Staff chat format component
        TextComponent text = Component.text()
                .append(senderText.color(NamedTextColor.GRAY))
                .append(Component.text(" > ").style(Style.style(NamedTextColor.DARK_GRAY, TextDecoration.BOLD)))
                .append(message)
                .build();

        send(text, !cmd);
    }

    public static void sendCommand(CommandSource source, Component msg){
        send(
                Component.text()
                        .append(CrownUtils.sourceDisplayName(source).color(NamedTextColor.GRAY))
                        .append(Component.text(": ").style(Style.style(NamedTextColor.DARK_GRAY)))
                        .append(msg)
                        .build()
                ,
                true
        );
    }

    public static void send(Component text, boolean log){
        Component message = Component.text()
                .append(PREFIX)
                .append(text)
                .build();

        for (Player p : Bukkit.getOnlinePlayers()){
            if(ignoring.contains(p)) continue;
            if(p.hasPermission("ftc.staffchat")) p.sendMessage(message);
        }

        if(log) Bukkit.getConsoleSender().sendMessage(message);
    }
}
