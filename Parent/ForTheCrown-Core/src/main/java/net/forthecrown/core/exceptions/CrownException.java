package net.forthecrown.core.exceptions;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import org.bukkit.command.CommandSender;

public class CrownException extends RuntimeException{

    public CrownException() {
        super(null, null, true, false);
    }

    private String message;
    public CrownException(CommandSender sender, String message) {
        super(CrownUtils.translateHexCodes(message), null, true, false);
        this.message = CrownUtils.translateHexCodes(message);
        sendMessage(sender, this.message);
    }

    public CrownException(String message){
        super(CrownUtils.translateHexCodes(message), null, true, false);
        this.message = CrownUtils.translateHexCodes(message);
    }

    public void sendMessage(CommandSender sender, String message){
        sender.sendMessage(ComponentUtils.convertString(message));
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
