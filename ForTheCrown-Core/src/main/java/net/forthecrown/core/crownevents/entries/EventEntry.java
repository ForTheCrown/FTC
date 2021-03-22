package net.forthecrown.core.crownevents.entries;

import org.bukkit.event.Listener;

/**
 * Represents info an event might need about a participant
 * Such as the timer attached to a player, their InEventListener or something similar
 * Makes keeping track of stuff in events with multiple concurrent participants easier
 */
public abstract class EventEntry {

    protected final Listener inEventListener;

    EventEntry(Listener inEventListener){
        this.inEventListener = inEventListener;
    }

    public Listener inEventListener() {
        return inEventListener;
    }
}
