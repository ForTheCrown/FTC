package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidPlayerInArgument extends InvalidCommandExecution{

    public InvalidPlayerInArgument(CommandSender sender){
        super(sender);
        super.sendMessage("Invalid player in argument!");
    }

    public InvalidPlayerInArgument(CommandSender sender, String player){
        super(sender);
        super.sendMessage(player + " is not a valid player");
    }
}
