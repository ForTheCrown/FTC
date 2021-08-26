package net.forthecrown.events;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.event.Event;

public interface ExceptionedListener<T extends Event> {
    void execute(T event) throws RoyalCommandException;
}
