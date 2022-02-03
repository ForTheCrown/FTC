package net.forthecrown.crownevents;

import net.forthecrown.crownevents.engine.ArenaBuildContext;
import net.forthecrown.crownevents.engine.ArenaBuilder;
import net.forthecrown.crownevents.engine.EventArena;
import net.forthecrown.crownevents.entries.EventEntry;

public abstract class MultiArenaEvent<T extends EventArena<E>, E extends EventEntry> implements CrownEvent<E> {
    private final EventArena[] arenas;
    protected final ArenaBuilder<T, E> builder;

    protected MultiArenaEvent(int maxArenas, ArenaBuilder<T, E> builder) {
        this.arenas = new EventArena[maxArenas];
        this.builder = builder;
    }

    public boolean isArenaFree(int index) {
        return arenas[index] == null;
    }

    public void setArena(int index, T t) {
        arenas[index] = t;
    }

    public T getArena(int index) {
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

    public T createAndSet(E entry, ArenaBuildContext context) {
        int free = findFreeArena();
        if(free == -1) return null;

        T t = builder.build(entry, context);
        setArena(free, t);

        return t;
    }
}
