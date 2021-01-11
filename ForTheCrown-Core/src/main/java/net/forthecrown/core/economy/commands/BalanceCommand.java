package net.forthecrown.core.economy.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.files.Balances;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }
        Player player = (Player) sender;
        Balances bals = Economy.getBalances();

        if(args.length < 1){
            player.sendMessage("Your balance: " + bals.getBalance(player.getUniqueId()));
            return true;
        }

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){
            e.printStackTrace();
            player.sendMessage(args[0] + " is not a valid playername");
            return false;
        }

        player.sendMessage(args[0] + "'s balance is: " + bals.getBalance(targetUUID));
        return true;
    }
}
