package net.forthecrown.emperor;

import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.ChatUtils;
import org.bukkit.command.CommandSender;

/**
 * A type of exception that just sends a message to a specified player
 * <p>Incompatible with Brigadier, still usable in events tho lol</p>
 * <p>It shouldn't appear in console either, since suppression is set to true</p>
 */
public class CrownException extends RuntimeException {

    private final String message;
    public CrownException(CommandSender sender, String message) {
        super(ChatFormatter.translateHexCodes(message));
        this.message = ChatFormatter.translateHexCodes(message);
        sendMessage(sender, this.message);
    }

    public void sendMessage(CommandSender sender, String message){
        sender.sendMessage(ChatUtils.convertString(message));
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
