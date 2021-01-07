package net.forthecrown.core.chat;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatEvents implements Listener {

    private final FtcCore main = FtcCore.getInstance();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();

        // Send where player died, but ignore world_void deaths.
        if (!loc.getWorld().getName().equalsIgnoreCase("world_void"))
            player.sendMessage(ChatColor.GRAY + "[FTC] You died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ".");

        main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "! " + ChatColor.RESET + player.getName() + " died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ", world=" + loc.getWorld().getName() + ".");
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();


        //If more than half the message has uppercase letters it makes it all lower case
        if(message.length() > 8){
            int upCastCharNumber = 0;
            for(int i = 0; i < message.length(); i++){
                if(Character.isUpperCase(message.charAt(i))) upCastCharNumber++;
            }
            if(upCastCharNumber > (message.length()/2) && !player.hasPermission("ftc.chatcaseignore")) {
                message = message.toLowerCase();
                message += "!";
                player.sendMessage("Refrain from using all caps messages");
            }
        }

        // Edit message to have emotes:
        if (player.hasPermission("ftc.donator3"))  message = Chat.replaceEmojis(message);

        event.setMessage(message);

        // Handle players with staffchat toggled on:
        if (Chat.getSCTPlayers().contains(player)) {
            event.setCancelled(true);

            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (!(onlinePlayer.hasPermission("ftc.staffchat"))) continue;
                String message1 = ChatColor.GRAY + "[Staff] " + playerName + ChatColor.GRAY + ChatColor.BOLD + " > " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message);
                onlinePlayer.sendMessage(message1);
                System.out.println(message1);
            }
            return;
        }


        // The sender is in the Senate world.
        if (player.getWorld().getName().contains("senate")) {
            event.setCancelled(true);
            event.getRecipients().clear();
            String prettyPlayerName;

            // Give everyone a yellow name in chat.
            switch (playerName)
            {
                case "Wout":
                case "BotulToxin":
                    prettyPlayerName = ChatColor.YELLOW + "" + playerName;
                    break;
                default:
                    prettyPlayerName = ChatColor.of("#FFFFA1") + "" + playerName;
            }

            for (Player senator : Bukkit.getWorld("world_senate").getPlayers()) {
                senator.sendMessage(prettyPlayerName + " " + ChatColor.GRAY + ChatColor.BOLD + ">" + ChatColor.RESET + " " + message);
            }
            main.getServer().getConsoleSender().sendMessage("[SENATE] " + playerName + " > " + message);
        }

        // The sender is not in the Senate world, remove all players in Senate world from recipients:
        else {
            List<Player> recipientsToRemove = new ArrayList<>();

            for (Player recipient : event.getRecipients()) if (recipient.getWorld().getName().contains("senate")) recipientsToRemove.add(recipient);
            for (Player playerToRemove : recipientsToRemove) event.getRecipients().remove(playerToRemove);
        }
    }
}
