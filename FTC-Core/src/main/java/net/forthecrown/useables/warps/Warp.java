package net.forthecrown.useables.warps;

import net.forthecrown.utils.SilentPredicate;
import net.forthecrown.serializer.Deletable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.useables.Checkable;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * A location to which one can warp using the /warp command
 */
public interface Warp extends
        Predicate<Player>, Checkable, Deletable,
        JsonSerializable, HoverEventSource<Component>, SilentPredicate<Player>,
        Keyed
{
    /**
     * Sets the paste
     * @param location The new paste
     */
    void setDestination(Location location);

    /**
     * Gets a the current paste of the warp
     * @return The warp's paste
     */
    Location getDestination();

    /**
     * Gets the warp's display name
     * @return This warp's display name
     */
    Component displayName();
}
