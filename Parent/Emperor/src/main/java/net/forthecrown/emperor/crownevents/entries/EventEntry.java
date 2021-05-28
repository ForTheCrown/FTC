package net.forthecrown.emperor.crownevents.entries;

import net.forthecrown.emperor.crownevents.InEventListener;
import org.bukkit.event.Listener;

/**
 * Represents info an event might need about a participant
 * Such as the timer attached to a player, their InEventListener or something similar
 * Makes keeping track of stuff in events with multiple concurrent participants easier
 */
public abstract class EventEntry<T extends EventEntry<T>> {

    protected final InEventListener<T> inEventListener;

    EventEntry(InEventListener<T> inEventListener){
        this.inEventListener = inEventListener;
    }

    public Listener inEventListener() {
        return inEventListener;
    }
}
