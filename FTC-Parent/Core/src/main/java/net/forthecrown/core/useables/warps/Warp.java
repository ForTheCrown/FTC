package net.forthecrown.core.useables.warps;

import net.forthecrown.core.utils.SilentPredicate;
import net.forthecrown.core.serializer.Deleteable;
import net.forthecrown.core.serializer.JsonSerializable;
import net.forthecrown.core.useables.Preconditionable;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface Warp extends Predicate<Player>, Preconditionable, Deleteable, JsonSerializable, HoverEventSource<Component>, SilentPredicate<Player>, Keyed {
    void setDestination(Location location);
    Location getDestination();

    Component displayName();
}
