package net.forthecrown.crownevents.engine;

import net.forthecrown.crownevents.entries.EventEntry;
import org.bukkit.entity.Player;

public interface TriggerAction<E extends EventEntry> {
    void run(Player colliding, E entry);
}
