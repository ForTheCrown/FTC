package net.forthecrown.core.events.customevents;

import net.forthecrown.core.utils.CrownUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Currently unused. If we ever take over Essentials' message functionality, this could be used
 */
public class MessageUserEvent extends Event implements Cancellable {

    private final CommandSender sender;
    private final CommandSender receiver;
    private final boolean translateColors;
    private final boolean formatEmojis;

    private String message;
    private String userFormattedMessage;
    private String senderFormattedMessage;

    private final static String messageFormat = "[%s -> %s] %s";

    private boolean cancelled;

    public MessageUserEvent(CommandSender sender, CommandSender receiver, String message, boolean translateColors, boolean formatEmojis) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.translateColors = translateColors;
        this.formatEmojis = formatEmojis;

        //format messages
        if(formatEmojis) message = CrownUtils.formatEmojis(message);
        if(translateColors) message = CrownUtils.translateHexCodes(message);

        userFormattedMessage = String.format(messageFormat, sender.getName(), receiver.getName(), message);
        senderFormattedMessage = String.format(messageFormat, receiver.getName(), sender.getName(), message);
    }

    public String getUserFormattedMessage() {
        return userFormattedMessage;
    }

    public void setUserFormattedMessage(String userFormattedMessage) {
        this.userFormattedMessage = userFormattedMessage;
    }

    public String getSenderFormattedMessage() {
        return senderFormattedMessage;
    }

    public void setSenderFormattedMessage(String senderFormattedMessage) {
        this.senderFormattedMessage = senderFormattedMessage;
    }

    public CommandSender getSender() {
        return sender;
    }

    public CommandSender getReceiver() {
        return receiver;
    }

    public boolean isTranslateColors() {
        return translateColors;
    }

    public boolean isFormatEmojis() {
        return formatEmojis;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
