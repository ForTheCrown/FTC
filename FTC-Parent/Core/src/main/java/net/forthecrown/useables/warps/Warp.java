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

public interface Warp extends
        Predicate<Player>, Preconditionable, Deletable,
        JsonSerializable, HoverEventSource<Component>, SilentPredicate<Player>,
        Keyed
{
    void setDestination(Location location);
    Location getDestination();

    Component displayName();
}
