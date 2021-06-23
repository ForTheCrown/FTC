package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.InEventListener;
import org.bukkit.entity.Player;

public class TimerEntry extends PlayerEntry<TimerEntry> {

    protected final EventTimer timer;

    public TimerEntry(Player user, InEventListener<TimerEntry> inEventListener, EventTimer timer) {
        super(user, inEventListener);

        this.timer = timer;
    }

    public EventTimer timer() {
        return timer;
    }
}
