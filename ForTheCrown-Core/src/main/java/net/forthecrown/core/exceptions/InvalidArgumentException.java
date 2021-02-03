package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidArgumentException extends InvalidCommandExecution {
    public InvalidArgumentException(CommandSender sender) {
        super(sender, "&7Invalid arguments!");
    }
    public InvalidArgumentException(CommandSender sender, String extraInfo) {
        super(sender, "&7Invalid arguments! &r" + extraInfo);
    }
}
