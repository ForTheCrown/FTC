package net.forthecrown.mayevent.arena;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class TriggerableEvent {

    private final Predicate<EventArena> required;
    private final Consumer<EventArena> toRun;
    private final boolean removeAfterExec;
    private final boolean anarchy;

    public TriggerableEvent(boolean removeAfterExec, boolean allowAnarchy, Predicate<EventArena> required, Consumer<EventArena> run) {
        this.removeAfterExec = removeAfterExec;
        this.required = required;
        this.anarchy = allowAnarchy;
        toRun = run;
    }

    public void poll(EventArena arena){
        if(required.test(arena) || (anarchy && arena.wave() > 30 && arena.random.nextBoolean())) trigger(arena);
    }

    private void trigger(EventArena arena) {
        toRun.accept(arena);
        if(removeAfterExec) arena.toRemove.add(this);
    }
}
