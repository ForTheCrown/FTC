package net.forthecrown.ftceventboss.entries;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.crownevents.EventTimer;
import org.bukkit.event.Listener;

public class TimerEntry extends EventEntry{

    private final EventTimer timer;

    protected TimerEntry(CrownUser user, Listener inEventListener, EventTimer timer) {
        super(user, inEventListener);
        this.timer = timer;
    }

    public EventTimer timer() {
        return timer;
    }
}
