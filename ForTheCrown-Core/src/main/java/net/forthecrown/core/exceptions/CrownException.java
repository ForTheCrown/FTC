package net.forthecrown.core.exceptions;

import net.forthecrown.core.FtcCore;
import org.bukkit.command.CommandSender;

public class CrownException extends RuntimeException{

    public CrownException() {
    }

    private String message;
    public CrownException(CommandSender sender, String message) {
        super(FtcCore.translateHexCodes(message));
        this.message = message;
        sendMessage(sender, message);
    }

    public void sendMessage(CommandSender sender, String message){
        sender.sendMessage(FtcCore.translateHexCodes(message));
    }

    @Override
    public void printStackTrace(){
    }

    @Override
    public String getMessage(){
        return message;
    }

    @Override
    public Throwable fillInStackTrace(){
        return null;
    }
}
