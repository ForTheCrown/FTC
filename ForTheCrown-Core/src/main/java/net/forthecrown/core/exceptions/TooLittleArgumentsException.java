package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class TooLittleArgumentsException extends InvalidCommandExecution{
    public TooLittleArgumentsException(CommandSender sender) {
        super(sender, "&7Too little arguments!");
    }
    public TooLittleArgumentsException(CommandSender sender, String extraInfo) {
        super(sender, "&7Too little arguments! &r" + extraInfo);
    }
}
