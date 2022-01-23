package net.forthecrown.crownevents;

import net.forthecrown.crownevents.engine.EventArena;
import net.forthecrown.crownevents.entries.EventEntry;

public abstract class MultiArenaEvent<E extends EventEntry> implements CrownEvent<E> {
    private final EventArena[] arenas;

    protected MultiArenaEvent(int maxArenas) {
        this.arenas = new EventArena[maxArenas];
    }

    public boolean isArenaFree(int index) {
        return arenas[index] == null;
    }

    public <T extends EventArena<E>> void setArena(int index, T arena) {
        arenas[index] = arena;
    }

    public <T extends EventArena<E>> T getArena(int index) {
        return (T) arenas[index];
    }

    public int findFreeArena() {
        for (int i = 0; i < arenas.length; i++) {
            if(isArenaFree(i)) return i;
        }

        return -1;
    }

    public boolean hasFreeArena() {
        return findFreeArena() != -1;
    }
}
