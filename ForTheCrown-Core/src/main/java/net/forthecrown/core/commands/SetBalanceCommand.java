package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class SetBalanceCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Sets a players balance
     *
     *
     * Valid usages of command:
     * - /setbalance <player> <amount>
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - Balances
     * - Economy: Economy.getBalances
     *
     * Author: Botul
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 2) return false;

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){ return false; }

        int amountToSet;
        try {
            amountToSet = Integer.parseInt(args[1]);
        } catch (Exception e){ return false; }

        FtcCore.getBalances().setBalance(targetUUID, amountToSet);
        return true;
    }
}
