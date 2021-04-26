package net.forthecrown.mayevent.arena;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class TriggerableEvent {

    private final Predicate<EventArena> required;
    private final Consumer<EventArena> toRun;
    private final boolean removeAfterExec;

    public TriggerableEvent(boolean removeAfterExec, Predicate<EventArena> required, Consumer<EventArena> run) {
        this.removeAfterExec = removeAfterExec;
        this.required = required;
        toRun = run;
    }

    public void poll(EventArena arena){
        if(required.test(arena)) trigger(arena);
    }

    private void trigger(EventArena arena) {
        toRun.accept(arena);
        if(removeAfterExec) arena.toRemove.add(this);
    }
}
