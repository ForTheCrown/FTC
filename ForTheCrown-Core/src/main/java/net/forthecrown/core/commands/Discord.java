package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Discord implements CommandExecutor {

    /*
    * Just sends the player the discord link
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
        return true;
    }
}
