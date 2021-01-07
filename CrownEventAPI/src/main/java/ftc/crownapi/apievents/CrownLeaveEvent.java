package ftc.crownapi.apievents;

import ftc.crownapi.types.CrownEventUser;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CrownLeaveEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final CrownEventUser user;
    private final Location preExitLocation;
    private final int userRecord;
    private final int finalScore;
    private final boolean hasTimer;
    private final boolean hasTimerCD;
    private final long timerScore;

    public CrownLeaveEvent(CrownEventUser user, Location preExitLocation, int userRecord, int finalScore){
        this.user = user;
        this.preExitLocation = preExitLocation;
        this.userRecord = userRecord;
        this.finalScore = finalScore;
        hasTimer = false;
        hasTimerCD = false;
        timerScore = 0;
    }
    public CrownLeaveEvent(CrownEventUser user, Location preExitLocation, boolean hasTimer, int timerScore){
        this.user = user;
        this.preExitLocation = preExitLocation;
        this.hasTimer = hasTimer;
        this.timerScore = timerScore;
        hasTimerCD = false;
        userRecord = 0;
        finalScore = 0;
    }
    public CrownLeaveEvent(CrownEventUser user, Location preExitLocation, long timerScore, boolean hasTimerCD){
        this.user = user;
        this.preExitLocation = preExitLocation;
        this.hasTimerCD = hasTimerCD;
        this.timerScore = timerScore;
        userRecord = 0;
        finalScore = 0;
        hasTimer = false;
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
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public CrownEventUser getUser() {
        return user;
    }
    public Location getPreExitLocation() {
        return preExitLocation;
    }
    public int getUserRecord() {
        return userRecord;
    }
    public int getFinalScore() {
        return finalScore;
    }
    public boolean getHasTimer() {
        return hasTimer;
    }
    public boolean getHasTimerCD() {
        return hasTimerCD;
    }
    public long getTimerScore() {
        return timerScore;
    }
}
