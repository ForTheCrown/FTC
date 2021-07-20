package net.forthecrown.august;

import net.forthecrown.august.listener.AugustInEventListener;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.crownevents.entries.SingleEntry;
import org.bukkit.entity.Player;

public class AugustEntry extends SingleEntry {

    private final AugustInEventListener listener;
    private final EventTimer timer;

    private int score;

    public AugustEntry(Player player) {
        super(player);

        this.listener = new AugustInEventListener(this, player);
        this.timer = new EventTimer(player, EventConstants.TIMER_FORMAT, plr -> A_Main.event.end(AugustEvent.currentEntry));
    }

    public EventTimer timer() {
        return timer;
    }

    public int score() {
        return score;
    }

    public void increment() {
        score++;
    }

    @Override
    public AugustInEventListener inEventListener() {
        return listener;
    }
}
