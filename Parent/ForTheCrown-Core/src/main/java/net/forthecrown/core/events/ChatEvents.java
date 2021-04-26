package net.forthecrown.core.events;

import net.forthecrown.core.StaffChat;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;
import java.util.logging.Level;

public class ChatEvents implements Listener {
    public static final World SENATE_WORLD = Objects.requireNonNull(Bukkit.getWorld("world_senate"));

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();

        // Tell the Player where they died, but ignore world_void deaths.
        String diedAt = "died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ".";
        if (!loc.getWorld().getName().equalsIgnoreCase("world_void"))
            player.sendMessage(ChatColor.GRAY + "[FTC] You " + diedAt);

        Announcer.log(Level.INFO, "! " + player.getName() + " " + diedAt);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();

        //If more than half the message has uppercase make it all lower case
        if(message.length() > 8 && !player.hasPermission("ftc.chatcaseignore")){
            int upCastCharNumber = 0;
            for(int i = 0; i < message.length(); i++){
                if(Character.isUpperCase(message.charAt(i))) upCastCharNumber++;
            }
            if(upCastCharNumber > (message.length()/2)) {
                message = StringUtils.capitalize(message.toLowerCase());
                message += "!";
                player.sendMessage("Refrain from using all caps messages.");
            }
        }

        // Edit message to have emotes:
        if (player.hasPermission("ftc.donator3"))  message = CrownUtils.formatEmojis(message);
  
        event.setMessage(message);

        // Handle players with staffchat toggled on:
        if (StaffChat.sctPlayers.contains(player)) {
            event.setCancelled(true);
            StaffChat.send(player, message, false);
            return;
        }


        // The sender is in the Senate world.
        if (player.getWorld().equals(SENATE_WORLD)) {
            event.setCancelled(true);
            event.getRecipients().clear();
            TextComponent.Builder cMessage = Component.text();

            TextColor color;

            // Give everyone a yellow name in chat.
            switch (playerName) {
                case "Wout":
                case "BotulToxin":
                    color = NamedTextColor.YELLOW;
                    break;
                default:
                    color = TextColor.fromHexString("#FFFFA1");
            }

            cMessage
                    .append(
                            Component.text(playerName)
                                    .color(color)
                                    .hoverEvent(UserManager.getUser(playerName))
                                    .clickEvent(ClickEvent.suggestCommand("/w " + playerName))
                    )
                    .append(Component.text(" > ")
                            .style(Style.style(NamedTextColor.GRAY, TextDecoration.BOLD))
                    )
                    .append(ComponentUtils.convertString(CrownUtils.translateHexCodes(message)));

            for (Player senator : SENATE_WORLD.getPlayers()) senator.sendMessage(cMessage);
            Announcer.log(SenateLevel.SENATE, playerName + " > " + message);
        }

        // The sender is not in the Senate world, remove all players in Senate world from recipients:
        else event.getRecipients().removeAll(SENATE_WORLD.getPlayers());
    }

    public static class SenateLevel extends Level {
        public static final SenateLevel SENATE = new SenateLevel();

        protected SenateLevel() {
            super("SENATE", 700);
        }
    }
}
