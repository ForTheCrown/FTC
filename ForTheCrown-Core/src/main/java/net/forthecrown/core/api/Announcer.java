package net.forthecrown.core.api;

import java.util.List;

/**
 * The class representing the ingame Announcer.
 */
public interface Announcer extends CrownFileManager {

    /**
     * Gets the delay between automatic announcements
     * @return The delay in ticks
     */
    long getDelay();

    /**
     * Sets the delay between automatic announcements
     * @param delay The new delay in ticks
     */
    void setDelay(long delay);

    /**
     * Gets the string list of announcements used by the AutoAnnouncer
     * @return The list of announcements
     */
    List<String> getAnnouncements();

    /**
     * Set the list of strings the AutoAnnouncer uses, the [FTC] prefix is automatic
     * @param announcements The new list the announcer will use
     */
    void setAnnouncements(List<String> announcements);

    /**
     * Stops the AutoAnnouncer
     */
    void stopAnnouncer();

    /**
     * Starts the AutoAnnouncer
     */
    void startAnnouncer();

    /**
     * Announces a message to everyone, even the console and players in the senate world
     * @param message the message to announce, hex colors are automatically translated
     */
    void announceToAll(String message);

    /**
     * Announces a message to every player, excluding senate world players and the console
     * @param message the message to announce, hex colors are automatically translated
     */
    void announce(String message);

}
