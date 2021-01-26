package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidPlayerInArgument extends InvalidCommandExecution{

    public InvalidPlayerInArgument(CommandSender sender){
        super(sender, "&cInvalid player in argument!");
    }

    public InvalidPlayerInArgument(CommandSender sender, String player){
        super(sender, "&4" + player + " &cis not a valid player");
    }
}
