package net.forthecrown.emperor;

import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;

/**
 * The class representing the ingame Announcer.
 */
public interface Announcer extends CrownSerializer<CrownCore> {

    static Announcer inst(){
        return Main.announcer;
    }

    /**
     * Gets the delay between automatic announcements
     * @return The delay in ticks
     */
    short getDelay();

    /**
     * Sets the delay between automatic announcements
     * @param delay The new delay in ticks
     */
    void setDelay(short delay);

    /**
     * Gets the string list of announcements used by the AutoAnnouncer
     * @return The list of announcements
     */
    List<Component> getAnnouncements();

    /**
     * Set the list of strings the AutoAnnouncer uses, the [FTC] prefix is automatic
     * @param announcements The new list the announcer will use
     */
    void setAnnouncements(List<Component> announcements);

    /**
     * Stops the AutoAnnouncer
     */
    void stop();

    /**
     * Starts the AutoAnnouncer
     */
    void start();

    /**
     * Announces a message to everyone, even the console and players in the senate world
     * @param message the message to announce, hex colors are automatically translated
     */
    void announceToAll(String message);

    void announceToAll(Component component);

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
        acLiteral(ChatFormatter.translateHexCodes(ChatFormatter.formatEmojis(message.toString())));
    }

    static void ac(Object... messages){
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
        Bukkit.getServer().sendMessage(ChatUtils.convertString(message.toString(), false));
    }

    /**
     * Broadcasts a message with the [FTC] prefix
     * @param message the message to broadcast
     */
    static void prefixAc(String message){
        if(message == null) message = "null";
        ac(CrownCore.getPrefix() + message);
    }

    /**
     * Logs a message in the console
     * @param level the level on which to log
     * @param message The message to log
     */
    static void log(Level level, String message){
        if(message == null) message = "null";
        CrownCore.logger().log(level, message);
    }

    void announce(Component message);

    void announce(Component message, String permission);

    void announceToAllRaw(Component message);

    Component formatMessage(Component message);

    /**
     *
     * @param message
     * @param permission
     */
    void announce(String message, @Nullable String permission);

    void announceRaw(Component message);

    void announceRaw(Component message, @Nullable String permission);

    /**
     * Logs or announces a debug message, won't broadcast if on actual server
     * @param message The message to log, gets toString'ed, or just prints "null" if null
     */
    static void debug(Object message){
        String stringMessage = message == null ? "null" : message.toString();

        if(CrownCore.inDebugMode()) acLiteral(stringMessage);
        else log(DebugLevel.DEBUG, stringMessage);
    }

    static <T> T debugAndReturn(T message){
        debug(message);
        return message;
    }

    class DebugLevel extends Level {
        public static DebugLevel DEBUG = new DebugLevel();
        protected DebugLevel() {
            super("DEBUG", 700);
        }
    }
}
