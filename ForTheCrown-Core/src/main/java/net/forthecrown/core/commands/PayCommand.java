package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.Balances;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Pays another player a set amount of money, removes the money from the player as well
     *
     *
     * Valid usages of command:
     * - /pay <player> <amount>
     *
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     *
     * Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command!");
            return true;
        }
        if(args.length != 2) return false;

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        UUID target;

        if(args[0].contains(player.getName())) return false;

        try {
            target = FtcCore.getOffOnUUID(args[0]);
            Bukkit.getOfflinePlayer(args[0]).getName();
        } catch (Exception e){
            player.sendMessage(ChatColor.GRAY + args[0] + " is not a valid player");
            return true;
        }

        int amountToPay;
        try {
            amountToPay = Integer.parseInt(args[1]);
        } catch (Exception e){
            return false;
        }

        if(amountToPay <= 0){
            player.sendMessage("You can't pay nothing/negative amounts");
            return true;
        }

        Balances bals = FtcCore.getBalances();

        if(bals.getBalance(playerUUID) < amountToPay){
            player.sendMessage(ChatColor.GRAY + "You're not able to afford that");
            return true;
        }

        //gives money to target
        bals.addBalance(target, amountToPay, true);

        //removes money from player
        bals.addBalance(playerUUID, -amountToPay);

        player.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + amountToPay + " Rhines " + ChatColor.GRAY + "to " + ChatColor.YELLOW + args[0]);
        try{
            Bukkit.getPlayer(target).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + amountToPay + " Rhines " + ChatColor.GRAY + "by " + ChatColor.YELLOW + player.getName());
        } catch (Exception e){
            return true;
        }

        return true;
    }
}
