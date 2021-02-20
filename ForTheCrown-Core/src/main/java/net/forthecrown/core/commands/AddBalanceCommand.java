package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AddBalanceCommand extends CrownCommand{

    public AddBalanceCommand() {
        super("addbalance", FtcCore.getInstance());

        setAliases("addbal", "addcash", "addbank");
        setPermission("ftc.commands.addbalance");
        setUsage("&7Usage: &r/addbalance <player> <amount>");
        register();
    }

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
     * - FtcCore: FtcCore.getBalances
     *
     * Author: Wout
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 2) return false;

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (Exception e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        int amountToAdd;
        try {
            amountToAdd = Integer.parseInt(args[1]);
        } catch (Exception e){ throw new InvalidArgumentException(sender, args[1] + " &7must be a number"); }

        FtcCore.getBalances().addBalance(targetUUID, amountToAdd, false);
        sender.sendMessage(args[0] + " now has " + FtcCore.getBalances().getBalance(targetUUID) + " Rhines");
        return true;
    }
}
