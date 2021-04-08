package net.forthecrown.core.exceptions;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import org.bukkit.command.CommandSender;

/**
 * A type of exception that just sends a message to a specified player
 * <p>Incompatible with Brigadier, still usable in events tho lol</p>
 * <p>It shouldn't appear in console either, since suppression is set to true</p>
 */
public class CrownException extends RuntimeException{

    public CrownException() {
        super(null, null, true, true);
    }

    private String message;
    public CrownException(CommandSender sender, String message) {
        super(CrownUtils.translateHexCodes(message), null, true, true);
        this.message = CrownUtils.translateHexCodes(message);
        sendMessage(sender, this.message);
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
