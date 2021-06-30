package net.forthecrown.core;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

/**
 * A type of exception that just sends a message to a specified player
 * <p>Incompatible with Brigadier, still usable in events tho lol</p>
 */
public class CrownException extends RuntimeException {

    private final Component message;
    private final CommandSender sender;

    public CrownException(CommandSender sender, String message) {
        this(sender, ChatUtils.convertString(message));
    }

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

    public static CrownException translatable(CommandSender sender, String key, ComponentLike... args){
        return new CrownException(sender, Component.translatable(key, args));
    }

    public static CrownException translatable(CommandSender sender, String key, TextColor color, ComponentLike... args){
        return new CrownException(sender, Component.translatable(key, color, args));
    }
}
