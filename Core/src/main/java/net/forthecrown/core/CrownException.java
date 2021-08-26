package net.forthecrown.core;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/**
 * A type of exception that just sends a message to a specified player
 * <p>Incompatible with Brigadier, still usable in events tho lol</p>
 * @deprecated This shit old, get rid of it
 */
@Deprecated
public class CrownException extends RuntimeException {

    private final Component message;
    private final CommandSender sender;

    @Deprecated
    public CrownException(CommandSender sender, String message) {
        this(sender, ChatUtils.convertString(message));
    }

    @Deprecated
    public CrownException(CommandSender sender, Component message) {
        super(ChatUtils.getString(message));

        this.message = message;
        this.sender = sender;

        sendMessage(message);
    }

    public void sendMessage(Component message){ sender.sendMessage(message); }

    @Override
    public void printStackTrace(){
    }

    @Override
    public String getMessage(){
        return ChatUtils.getString(message);
    }

    public Component getComponentMessage(){
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
