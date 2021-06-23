package net.forthecrown.julyevent;

import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.entries.PlayerEntry;
import net.forthecrown.julyevent.listener.JulyEventListener;
import org.bukkit.entity.Player;

public class JulyEntry extends PlayerEntry<JulyEntry> {
    private final EventTimer timer;
    private final int bitSetIndex;

    long startTime;
    long endTime;

    public JulyEntry(Player user, EventTimer timer, int bitSetIndex) {
        super(user, new JulyEventListener(user));

        this.timer = timer;
        this.bitSetIndex = bitSetIndex;

        inEventListener.setEntry(this);
        inEventListener.register(JulyMain.inst);
    }

    public EventTimer timer() {
        return timer;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    public int bitSetIndex() {
        return bitSetIndex;
    }

    @Override
    public JulyEventListener inEventListener() {
        return (JulyEventListener) inEventListener;
    }
}
