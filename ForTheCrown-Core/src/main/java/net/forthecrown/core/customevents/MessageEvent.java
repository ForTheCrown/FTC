package net.forthecrown.core.customevents;

import net.forthecrown.core.api.CrownUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MessageEvent extends Event implements Cancellable {

    private final CrownUser receiver;
    private String message;
    private StringBuilder messageFormat;
    private final boolean seenBySocialSpy;

    private boolean cancelled;

    protected MessageEvent(CrownUser receiver, String message, boolean seenBySocialSpy){
        this.receiver =receiver;
        this.message = message;
        this.seenBySocialSpy = seenBySocialSpy;

        messageFormat = new StringBuilder();
        messageFormat.append("[").append("%USER1%").append(" -> ").append("%USER2%").append("] ").append(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StringBuilder getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(StringBuilder messageFormat) {
        this.messageFormat = messageFormat;
    }

    public CrownUser getReceiver() {
        return receiver;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
