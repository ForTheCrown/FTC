package net.forthecrown.july;

import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.entries.PlayerEntry;
import net.forthecrown.july.listener.OnTrackListener;
import org.bukkit.entity.Player;

public class ParkourEntry extends PlayerEntry<ParkourEntry> {
    private final EventTimer timer;
    private final int arenaIndex;

    long startTime;

    public ParkourEntry(Player user, EventTimer timer, int bitSetIndex) {
        super(user, new OnTrackListener(user, false));

        this.timer = timer;
        this.arenaIndex = bitSetIndex;

        inEventListener.setEntry(this);
    }

    public EventTimer timer() {
        return timer;
    }

    public long startTime() {
        return startTime;
    }

    public int arenaIndex() {
        return arenaIndex;
    }

    @Override
    public OnTrackListener inEventListener() {
        return (OnTrackListener) inEventListener;
    }
}
