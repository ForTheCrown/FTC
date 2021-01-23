package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Broadcasts a message to the entire server
     *
     *
     * Valid usages of command:
     * - /broadcast
     * - /bc
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     *
     * Author: Wout
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) return false;

        String message = FtcCore.getPrefix();
        message += String.join(" ", args); //joins the args array into one string
        message = FtcCore.replaceEmojis(message); //replaces any emojis in the message
        message = FtcCore.translateHexCodes(message); //adds chatcolor

        Bukkit.broadcastMessage(message);
        return true;
    }
}
