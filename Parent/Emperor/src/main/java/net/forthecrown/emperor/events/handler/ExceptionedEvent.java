package net.forthecrown.emperor.events.handler;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.event.Event;

public interface ExceptionedEvent<T extends Event> {
    void onEvent(T event) throws RoyalCommandException;
}
