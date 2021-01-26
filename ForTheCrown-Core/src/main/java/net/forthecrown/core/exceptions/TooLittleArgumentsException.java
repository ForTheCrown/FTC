package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class TooLittleArgumentsException extends InvalidCommandExecution{
    public TooLittleArgumentsException(CommandSender sender) {
        super(sender, "&cToo little arguments!");
    }
    public TooLittleArgumentsException(CommandSender sender, String extraInfo) {
        super(sender, "&cToo little arguments! &r" + extraInfo);
    }
}
