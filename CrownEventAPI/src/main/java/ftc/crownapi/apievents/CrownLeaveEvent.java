package ftc.crownapi.apievents;

import ftc.crownapi.types.CrownEventUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnegative;

public class CrownLeaveEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final CrownEventUser user;
    private final int userRecord;
    private final int finalScore;

    public CrownLeaveEvent(final CrownEventUser user, final int userRecord, @Nonnegative final int finalScore){
        this.user = user;
        this.userRecord = userRecord;
        this.finalScore = finalScore;
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
    public int getUserRecord() {
        return userRecord;
    }

    public int getFinalScore() {
        return finalScore;
    }
}
