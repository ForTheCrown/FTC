package net.forthecrown.crownevents.entries;

import net.forthecrown.crownevents.engine.EventArena;

public interface ArenaEntry<T extends EventArena> extends EventEntry {
    /**
     * Attempts to set the arena the entry uses
     * @param arena The arena to set
     * @throws IllegalStateException If the entry already has an assigned arena
     */
    void setArena(T arena) throws IllegalStateException;

    /**
     * Gets the arena this entry is using
     * @return The entry's arena
     */
    T arena();
}
