package net.forthecrown.valhalla.data.triggers;

import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.valhalla.active.ActiveRaid;
import org.bukkit.event.Event;

import java.util.function.Predicate;

public interface TriggerInstance<E extends Event> extends Predicate<E>, JsonSerializable {
    void execute(E context, ActiveRaid raid);

    boolean removeAfterExec();
}
