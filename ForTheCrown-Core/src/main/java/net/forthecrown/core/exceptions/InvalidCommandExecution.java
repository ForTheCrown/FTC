package net.forthecrown.core.exceptions;

import net.forthecrown.core.FtcCore;
import org.bukkit.command.CommandSender;

public class InvalidCommandExecution extends RuntimeException{

    private final CommandSender sender;
    public InvalidCommandExecution(CommandSender sender){
        this.sender = sender;
    }

    public void sendMessage(String s){
        sender.sendMessage(FtcCore.translateHexCodes(s));
    }
}
