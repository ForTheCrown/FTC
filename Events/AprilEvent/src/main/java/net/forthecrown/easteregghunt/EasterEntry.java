package net.forthecrown.easteregghunt;

import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.entries.TimerEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class EasterEntry extends TimerEntry {

    private int score = 0;

    public EasterEntry(Player user, Listener inEventListener, EventTimer timer) {
        super(user, inEventListener, timer);
    }

    public void inc(){
        score += 10;
    }

    public void dec(){
        score -= 10;
    }

    public int score() {
        return score;
    }
}
