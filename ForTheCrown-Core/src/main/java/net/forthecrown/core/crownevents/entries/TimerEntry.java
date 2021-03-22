package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.crownevents.EventTimer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TimerEntry extends SingleEntry {

    protected final EventTimer timer;

    public TimerEntry(Player user, Listener inEventListener, EventTimer timer) {
        super(user, inEventListener);

        this.timer = timer;
    }

    public EventTimer timer() {
        return timer;
    }
}
