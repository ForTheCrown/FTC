package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class LeaveVanishCommand implements CrownCommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Makes the executor or player go into vanish and display a left the game message
     *
     *
     * Valid usages of command:
     * - /leavevansih
     * - /leavevanish <player>
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - (Essentials)
     *
     * Author: Botul
     */

    Set<Player> vanishedPlayer = new HashSet<>();

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

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
            } catch (Exception e) { throw new InvalidPlayerInArgument(sender, args[0]); }

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
