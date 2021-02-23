package net.forthecrown.core.api;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;

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

    /**
     * Broadcasts a message, used for debugging
     * @param message The message to broadcasts
     */
    static void ac(String message){
        acLiteral(CrownUtils.translateHexCodes(CrownUtils.formatEmojis(message)));
    }

    static void acLiteral(String message){
        for (Player p: Bukkit.getOnlinePlayers()){
            p.sendMessage(message);
        }
        System.out.println(message);
    }

    /**
     * Logs a message in the console
     * @param level the level on which to log
     * @param message The message to log
     */
    static void log(Level level, String message){
        FtcCore.getInstance().getLogger().log(level, message);
    }

    /**
     *
     * @param message
     * @param permission
     */
    void announceToAllWithPerms(String message, @Nullable String permission);
}
