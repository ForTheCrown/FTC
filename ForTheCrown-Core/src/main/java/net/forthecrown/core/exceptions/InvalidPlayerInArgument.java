package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidPlayerInArgument extends InvalidCommandExecution{

    public InvalidPlayerInArgument(CommandSender sender){
        super(sender, "&7Invalid player in argument!");
    }

    public InvalidPlayerInArgument(CommandSender sender, String player){
        super(sender, "&8" + player + " &7is not a valid player");
    }

    public InvalidPlayerInArgument(CommandSender sender, String player, String extraInfo){
        super(sender, "&8" + player + " &7is not a valid player &r" + extraInfo);
    }
}
