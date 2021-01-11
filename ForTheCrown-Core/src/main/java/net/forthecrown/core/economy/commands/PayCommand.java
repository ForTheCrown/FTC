package net.forthecrown.core.economy.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.files.Balances;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        if(args.length != 2) return false;

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        UUID target;

        try {
            target = FtcCore.getOffOnUUID(args[0]);
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

        if(amountToPay < 0){
            player.sendMessage("You can't pay someone negative amounts");
            return true;
        }

        Balances bals = Economy.getBalances();

        if(bals.getBalance(player.getUniqueId()) < amountToPay){
            player.sendMessage(ChatColor.GRAY + "You're not able to afford that");
            return true;
        }

        //gives money to target
        bals.setBalance(target, bals.getBalance(target) + amountToPay);

        //removes money from player
        bals.setBalance(playerUUID, bals.getBalance(playerUUID) - amountToPay);

        player.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + amountToPay + " Rhines" + ChatColor.GRAY + "to " + ChatColor.YELLOW + args[0]);
        try{
            Bukkit.getPlayer(target).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + amountToPay + " Rhines" + ChatColor.GRAY + "by " + ChatColor.YELLOW + player.getName());
        } catch (Exception e){
            return true;
        }

        return true;
    }
}
