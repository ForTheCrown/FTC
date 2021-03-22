package net.forthecrown.ftceventboss.entries;

import net.forthecrown.core.api.CrownUser;
import org.bukkit.event.Listener;

public abstract class EventEntry {

    private final CrownUser user;
    private final Listener inEventListener;

    protected EventEntry(CrownUser user, Listener inEventListener){
        this.user = user;
        this.inEventListener = inEventListener;
    }

    public CrownUser user() {
        return user;
    }

    public Listener inEventListener() {
        return inEventListener;
    }
}
