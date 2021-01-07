package ftc.crownapi.apievents;

import ftc.crownapi.types.CrownEventUser;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CrownEnterEvent extends Event implements Cancellable {

    private final CrownEventUser user;
    private final Location preEntryLoc;
    private final boolean hasTimer;
    private final int timeInSeconds;

    boolean cancelled;

    public CrownEnterEvent (CrownEventUser user, Location preEntryLocation, boolean hasTimer){
        this.user = user;
        this.preEntryLoc = preEntryLocation;
        this.hasTimer = hasTimer;
        hasTimerCountingDown = false;
        timeInSeconds = 0;
    }
    public CrownEnterEvent (CrownEventUser user, Location preEntryLocation, boolean hasTimerCountingDown, int timeInSeconds){
        this.user = user;
        this.preEntryLoc = preEntryLocation;
        this.hasTimerCountingDown = hasTimerCountingDown;
        this.timeInSeconds = timeInSeconds;
        hasTimer = false;
    }

    public CrownEventUser getUser(){
        return user;
    }
    public Location getPreEntryLoc(){
        return preEntryLoc;
    }
    public boolean getHasTimer() {
        return hasTimer;
    }
    public boolean getHasTimerCountingDown() {
        return hasTimerCountingDown;
    }
    public int getTimeInSeconds() {
        return timeInSeconds;
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
