package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidArgumentException extends InvalidCommandExecution {
    public InvalidArgumentException(CommandSender sender) {
        super(sender);
        super.sendMessage("&4Invalid arguments!");
    }
    public InvalidArgumentException(CommandSender sender, String extraInfo) {
        super(sender);
        super.sendMessage("&4Invalid arguments! &r" + extraInfo);
    }
}
