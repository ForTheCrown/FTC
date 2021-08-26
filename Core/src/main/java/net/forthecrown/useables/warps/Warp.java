package net.forthecrown.useables.warps;

import net.forthecrown.utils.SilentPredicate;
import net.forthecrown.serializer.Deletable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.useables.Preconditionable;
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
        Predicate<Player>, Preconditionable, Deletable,
        JsonSerializable, HoverEventSource<Component>, SilentPredicate<Player>,
        Keyed
{
    /**
     * Sets the destination
     * @param location The new destination
     */
    void setDestination(Location location);

    /**
     * Gets a the current destination of the warp
     * @return The warp's destination
     */
    Location getDestination();

    /**
     * Gets the warp's display name
     * @return This warp's display name
     */
    Component displayName();
}
