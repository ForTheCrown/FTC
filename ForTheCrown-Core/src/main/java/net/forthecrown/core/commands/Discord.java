package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Discord implements CrownCommandExecutor {

    /*
    * Just sends the player the discord link
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException {
        sender.sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
        return true;
    }
}
