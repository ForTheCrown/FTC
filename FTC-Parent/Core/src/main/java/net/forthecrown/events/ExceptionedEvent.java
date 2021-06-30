package net.forthecrown.events;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.event.Event;

public interface ExceptionedEvent<T extends Event> {
    void execute(T event) throws RoyalCommandException;
}
