package net.forthecrown.core.exceptions;

import org.bukkit.command.CommandSender;

public class InvalidCommandExecution extends CrownException{
    public InvalidCommandExecution(CommandSender sender, String message){
        super(sender, message);
    }
}
