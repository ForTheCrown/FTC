package net.forthecrown.core.exceptions;

import net.forthecrown.core.CrownUtils;
import org.bukkit.command.CommandSender;

public class CrownException extends RuntimeException{

    public CrownException() {
    }

    public CrownException(CommandSender sender, String message) {
        super(CrownUtils.translateHexCodes(message));
        sendMessage(sender, message);
    }

    public void sendMessage(CommandSender sender, String message){
        sender.sendMessage(CrownUtils.translateHexCodes(message));
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
