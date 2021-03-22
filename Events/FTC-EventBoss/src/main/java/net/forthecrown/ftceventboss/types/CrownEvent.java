package net.forthecrown.ftceventboss.types;

import net.forthecrown.ftceventboss.entries.EventEntry;
import org.bukkit.entity.Player;

public interface CrownEvent {

    void start(Player player);
    void end(EventEntry entry);
    void complete(EventEntry entry);

}
