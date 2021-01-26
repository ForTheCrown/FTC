package net.forthecrown.core.exceptions;

import net.forthecrown.core.FtcCore;
import org.bukkit.command.CommandSender;

public class CrownException extends RuntimeException{

    public CrownException() {
    }

    public void sendMessage(CommandSender sender, String message){
        sender.sendMessage(FtcCore.translateHexCodes(message));
    }

    @Override
    public void printStackTrace(){
    }

    @Override
    public String getMessage(){
        return null;
    }

    @Override
    public Throwable fillInStackTrace(){
        return null;
    }
}
