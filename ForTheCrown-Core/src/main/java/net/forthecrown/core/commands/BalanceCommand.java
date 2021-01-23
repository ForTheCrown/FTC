package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.Balances;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Displays either the executors balance or another players balance
     *
     *
     * Valid usages of command:
     * - /balance
     * - /balance <player>
     *
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     * - FtcCore: FtcCore.getOnOffUUID
     *
     * Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }
        Player player = (Player) sender;
        Balances bals = FtcCore.getBalances();

        if(args.length < 1){
            player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + bals.getBalance(player.getUniqueId()) +" Rhines");
            return true;
        }

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){
            e.printStackTrace();
            player.sendMessage(ChatColor.GRAY + args[0] + " is not a valid playername");
            return false;
        }

        player.sendMessage(ChatColor.GOLD + "$ " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " currently has " + ChatColor.GOLD + bals.getBalance(targetUUID) + " Rhines");
        return true;
    }
}
