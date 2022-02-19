package net.forthecrown.core.chat;

import com.google.common.base.Predicates;
import net.forthecrown.serializer.CrownSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

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

    default void announce(Component message) { announceRaw(formatMessage(message), Predicates.alwaysTrue()); }
    default void announce(Component message, Permission permission) { announce(message, permission.getName()); }
    default void announce(Component message, String permission) { announceRaw(formatMessage(message), plr -> plr.hasPermission(permission)); }

    default void announceToAll(Component message) { announceToAllRaw(formatMessage(message), Predicates.alwaysTrue()); }
    default void announceToAll(Component message, Permission perm) { announceToAll(message, perm.getName()); }
    default void announceToAll(Component message, String permission) { announceToAllRaw(formatMessage(message), plr -> plr.hasPermission(permission)); }

    default void announceRaw(Component message) { announceRaw(message, Predicates.alwaysTrue()); }
    default void announceRaw(Component message, Permission permission) { announce(message, permission.getName()); }
    default void announceRaw(Component message, String permission) { announceRaw(message, plr -> plr.hasPermission(permission)); }

    default void announceToAllRaw(Component message) { announceToAllRaw(message, Predicates.alwaysTrue()); }
    default void announceToAllRaw(Component message, Permission perm) { announceToAll(message, perm.getName()); }
    default void announceToAllRaw(Component message, String permission) { announceToAllRaw(message, plr -> plr.hasPermission(permission)); }

    void announceRaw(Component announcement, @Nullable Predicate<Player> predicate);
    void announceToAllRaw(Component announcement, @Nullable Predicate<CommandSender> predicate);

    Component formatMessage(Component message);
}
