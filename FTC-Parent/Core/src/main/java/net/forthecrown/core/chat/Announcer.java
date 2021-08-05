package net.forthecrown.core.chat;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * The class representing the ingame Announcer.
 * <p></p>
 * Implementation: {@link FtcAnnouncer}
 */
public interface Announcer extends CrownSerializer {

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
     * Adds an announcement
     * @param announcement the announcement to add
     */
    void add(Component announcement);

    /**
     * Removes an announcement
     * @param acIndex The index of the announcement to remove
     */
    void remove(int acIndex);

    /**
     * Stops the AutoAnnouncer
     */
    void stop();

    /**
     * Starts the AutoAnnouncer
     */
    void start();

    /**
     * Broadcasts a message without formatting hex colors or emojis
     * @param message The message to broadcast
     */
    static void acLiteral(Object message){
        if(message == null) message = "null";
        Bukkit.getServer().sendMessage(ChatUtils.convertString(message.toString(), false));
    }

    /**
     * Logs a message in the console
     * @param level the level on which to log
     * @param message The message to log
     */
    static void log(Level level, String message){
        if(message == null) message = "null";
        ForTheCrown.logger().log(level, message);
    }

    default void announce(Component message) { announceRaw(formatMessage(message), FtcUtils.alwaysAccept()); }
    default void announce(Component message, Permission permission) { announce(message, permission.getName()); }
    default void announce(Component message, String permission) { announceRaw(formatMessage(message), plr -> plr.hasPermission(permission)); }

    default void announceToAll(Component message) { announceToAllRaw(formatMessage(message), FtcUtils.alwaysAccept()); }
    default void announceToAll(Component message, Permission perm) { announceToAll(message, perm.getName()); }
    default void announceToAll(Component message, String permission) { announceToAllRaw(formatMessage(message), plr -> plr.hasPermission(permission)); }

    default void announceRaw(Component message) { announceRaw(message, FtcUtils.alwaysAccept()); }
    default void announceRaw(Component message, Permission permission) { announce(message, permission.getName()); }
    default void announceRaw(Component message, String permission) { announceRaw(message, plr -> plr.hasPermission(permission)); }

    default void announceToAllRaw(Component message) { announceToAllRaw(message, FtcUtils.alwaysAccept()); }
    default void announceToAllRaw(Component message, Permission perm) { announceToAll(message, perm.getName()); }
    default void announceToAllRaw(Component message, String permission) { announceToAllRaw(message, plr -> plr.hasPermission(permission)); }

    void announceRaw(Component announcement, @Nullable Predicate<Player> predicate);
    void announceToAllRaw(Component announcement, @Nullable Predicate<CommandSender> predicate);

    Component formatMessage(Component message);

    /**
     * Logs or announces a debug message, won't broadcast if on actual server
     * @param message The message to log, gets toString'ed, or just prints "null" if null
     */
    static void debug(Object message){
        String stringMessage = message == null ? "null" : message.toString();

        if(ForTheCrown.inDebugMode()) acLiteral(stringMessage);
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
