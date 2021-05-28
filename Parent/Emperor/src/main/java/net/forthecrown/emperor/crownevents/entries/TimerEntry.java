package net.forthecrown.emperor.crownevents.entries;

import net.forthecrown.emperor.crownevents.EventTimer;
import net.forthecrown.emperor.crownevents.InEventListener;
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
