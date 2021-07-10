package net.forthecrown.crownevents;

import net.forthecrown.crownevents.entries.EventEntry;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.entity.Player;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

/**
 * A basic CrownEvent interface
 * @param <T> The type of EventEntry this event accepts
 */
public interface CrownEvent<T extends EventEntry> {

    /**
     * Starts the Event for the specified player
     * <p>Note: the start method should also create an event entry for the player</p>
     * @param player The player to start the event for
     */
    void start(Player player) throws RoyalCommandException;

    /**
     * Method to call when ending an event
     * @param entry The entry to end the event for
     */
    void end(T  entry);

    /**
     * Method for when someone passes an event successfully
     * @param entry The entry that completed the event
     */
    void complete(T entry);

    default String getName(){
        return "CrownEvent_" + Month.of(new Date().getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
