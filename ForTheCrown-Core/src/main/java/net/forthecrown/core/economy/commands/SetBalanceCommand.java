package net.forthecrown.core.economy.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.FtcUserData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class SetBalanceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 2) return false;

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){
            sender.sendMessage( args[0] + " is not a valid player");
            return false;
        }

        int amountToSet;
        try {
            amountToSet = Integer.parseInt(args[1]);
        } catch (Exception e){
            sender.sendMessage("The Amount to set must be a number");
            return false;
        }

        FtcUserData targetData = FtcCore.getUserData(targetUUID);
        targetData.setBalance(amountToSet);
        return true;
    }
}
