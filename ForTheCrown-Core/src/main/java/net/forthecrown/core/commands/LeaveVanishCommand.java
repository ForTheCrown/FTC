package net.forthecrown.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class LeaveVanishCommand implements CommandExecutor {

    Set<Player> vanishedPlayer = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only player may execute this command!");
            return true;
        }

        Player player = (Player) sender;

        if(args.length < 1){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender() ,"vanish " + player.getName());

            if(!vanishedPlayer.contains(player)){
                Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " left the game");
                player.sendMessage("You've been placed in vanish and everyone thinks you've left");

                vanishedPlayer.add(player);
            }else {
                Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " joined the game");
                player.sendMessage("You've been removed from vanish");
            }
        } else {
            Player target;
            try {
                target = Bukkit.getPlayer(args[0]);
            } catch (Exception e) {
                return false;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender() ,"vanish " + target.getName());

            if(!vanishedPlayer.contains(target)){
                Bukkit.broadcastMessage(ChatColor.YELLOW + target.getName() + " left the game");

                player.sendMessage(target.getName() + " been placed in vanish and everyone thinks they've left");
                target.sendMessage("You've been placed in vanish and everyone thinks you've left");

                vanishedPlayer.add(target);
            } else {
                Bukkit.broadcastMessage(ChatColor.YELLOW + target.getName() + " joined the game");

                target.sendMessage("You've been removed from vanish");
                player.sendMessage(target.getName() + " been removed vanish");
            }
        }
        return true;
    }
}
