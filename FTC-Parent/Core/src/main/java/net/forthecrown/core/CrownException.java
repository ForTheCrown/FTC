package net.forthecrown.core;

import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.ChatUtils;
import org.bukkit.command.CommandSender;

/**
 * A type of exception that just sends a message to a specified player
 * <p>Incompatible with Brigadier, still usable in events tho lol</p>
 */
public class CrownException extends RuntimeException {

    private final String message;
    private final CommandSender sender;

    public CrownException(CommandSender sender, String message) {
        super(ChatFormatter.translateHexCodes(message));

        this.message = ChatFormatter.translateHexCodes(message);
        this.sender = sender;

        sendMessage(getMessage());
    }

    public void sendMessage(String message){
        sender.sendMessage(ChatUtils.convertString(message));
    }

    @Override
    public void printStackTrace(){
    }

    @Override
    public String getMessage(){
        return message;
    }

    public CommandSender getSender() {
        return sender;
    }

    @Override
    public Throwable fillInStackTrace(){
        return null;
    }
}
