package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AddBalanceCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds to a players balance
     *
     *
     * Valid usages of command:
     * - /addbalance <player> <amount>
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - Balances
     * - Economy: Economy.getBalances
     *
     * Author: Wout
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 2) return false;

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){
            sender.sendMessage( args[0] + " is not a valid player");
            return true;
        }

        int amountToAdd;
        try {
            amountToAdd = Integer.parseInt(args[1]);
        } catch (Exception e){
            sender.sendMessage("The Amount to add must be a number");
            return true;
        }

        FtcCore.getBalances().addBalance(targetUUID, amountToAdd);
        sender.sendMessage(args[0] + " now has " + FtcCore.getBalances().getBalance(targetUUID) + " Rhines");
        return true;
    }
}
