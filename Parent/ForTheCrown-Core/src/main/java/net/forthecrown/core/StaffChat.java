package net.forthecrown.core;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Class representing the staff chat
 * <p>Exists because I was cleaning up the ChatEvents class lol</p>
 */
public class StaffChat {
    private static Set<Player> sctPlayers = new HashSet<>();

    /**
     * Sends a staff chat message
     * @param sender The message's sender
     * @param message The message
     * @param cmd Whether the message was sent via command (If true, message won't get logged)
     */
    public static void send(@NotNull CommandSender sender, @NotNull String message, boolean cmd){
        Validate.notNull(sender, "Sender was null");

        TextComponent senderText = Component.text(sender.getName()).color(NamedTextColor.GRAY);
        if(sender instanceof Player || sender instanceof CrownUser){
            //Sender is player, make text hover event
            CrownUser user = UserManager.getUser(sender.getName());
            senderText = senderText
                    .hoverEvent(user.asHoverEvent())
                    .clickEvent(ClickEvent.suggestCommand("/w " + user.getName()));
        }

        //Staff chat format component
        TextComponent text = Component.text()
                .append(Component.text("[Staff] ").color(NamedTextColor.DARK_GRAY))
                .append(senderText)
                .append(Component.text(" > ").style(Style.style(NamedTextColor.DARK_GRAY, TextDecoration.BOLD)))
                .append(ComponentUtils.convertString(CrownUtils.formatEmojis(message)))
                .build();

        //Send message to all players with sc perms
        for (Player p : Bukkit.getOnlinePlayers()){
            if(p.hasPermission("ftc.staffchat")) p.sendMessage(text);
        }
        //If the message is being sent by command, don't log it
        if(!cmd) Bukkit.getConsoleSender().sendMessage(text);
    }

    /**
     * Gets every player with staffChatToggle on
     * @return staff chat toggle players
     */
    public static Set<Player> getSCT(){ //gets a list of all the players, whose messages will always go to staffchat
        return sctPlayers;
    }
    public static void setSCT(Set<Player> sctPlayers){
        StaffChat.sctPlayers = sctPlayers;
    }
}
