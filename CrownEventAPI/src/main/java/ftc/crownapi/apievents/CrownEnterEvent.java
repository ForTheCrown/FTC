package ftc.crownapi.apievents;

import ftc.crownapi.types.CrownEventUser;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CrownEnterEvent extends Event implements Cancellable {

    private final CrownEventUser user;
    private final String eventName;
    private final Location preEntryLoc;

    boolean cancelled;

    public CrownEnterEvent (CrownEventUser user, String eventName, Location preEntryLocation){
        this.user = user;
        this.eventName = eventName;
        this.preEntryLoc = preEntryLocation;
    }

    public String getEventName(){
        return eventName;
    }
    public CrownEventUser getUser(){
        return user;
    }
    public Location getPreEntryLoc(){
        return preEntryLoc;
    }


    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
