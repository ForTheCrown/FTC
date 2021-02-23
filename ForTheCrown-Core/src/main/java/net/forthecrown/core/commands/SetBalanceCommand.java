package net.forthecrown.core.commands;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.TooLittleArgumentsException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SetBalanceCommand extends CrownCommand {
    public SetBalanceCommand(){
        super("setbalance", FtcCore.getInstance());

        setAliases("setbal", "setcash", "setbank", "setmoney");
        setDescription("Sets a players balance.");
        register();
    }

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
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length != 2) throw new TooLittleArgumentsException(sender);

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        int amountToSet;
        try {
            amountToSet = Integer.parseInt(args[1]);
        } catch (Exception e){ throw new InvalidArgumentException(sender, args[1] + " is not a number"); }

        FtcCore.getBalances().setBalance(targetUUID, amountToSet);
        sender.sendMessage(CrownUtils.translateHexCodes("&e" + args[0] + " &7now has &6" + amountToSet + " Rhines"));
        return true;
    }
}
