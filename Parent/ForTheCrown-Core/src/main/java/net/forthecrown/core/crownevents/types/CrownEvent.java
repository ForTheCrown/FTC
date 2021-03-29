package net.forthecrown.core.crownevents.types;

import net.forthecrown.core.crownevents.entries.EventEntry;
import org.bukkit.entity.Player;

//T is the accepted type of event entry, I'm prettu sure you could still pass in super classes for specified types lol
public interface CrownEvent<T extends EventEntry> {
    void start(Player player);

    //Blanket method for event ending, could be called by complete()
    void end(T  entry);

    //When the event is completed successfully
    void complete(T entry);

}
