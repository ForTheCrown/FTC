package net.forthecrown.core.api;

import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.FtcCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class representing the ingame Announcer.
 */
public interface Announcer extends CrownSerializer<FtcCore> {

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
     * @param message The message to broadcast
     */
    static void ac(Object message){
        if(message == null) message = "null";
        acLiteral(CrownUtils.translateHexCodes(CrownUtils.formatEmojis(message.toString())));
    }

    static void ac(Object[] messages){
        for (Object o: messages){
            ac(o);
        }
    }

    /**
     * Broadcasts a message without formatting hex colors or emojis
     * @param message The message to broadcast
     */
    static void acLiteral(Object message){
        if(message == null) message = "null";
        for (Player p: Bukkit.getOnlinePlayers()){
            p.sendMessage(message.toString());
        }
        System.out.println(message);
    }

    /**
     * Broadcasts a message with the [FTC] prefix
     * @param message the message to broadcast
     */
    static void prefixAc(String message){
        if(message == null) message = "null";
        ac(FtcCore.getPrefix() + message);
    }

    /**
     * Logs a message in the console
     * @param level the level on which to log
     * @param message The message to log
     */
    static void log(Level level, String message){
        if(message == null) message = "null";
        logger.log(level, message);
    }

    Logger logger = FtcCore.getInstance().getLogger();

    /**
     *
     * @param message
     * @param permission
     */
    void announce(String message, @Nullable String permission);
}
