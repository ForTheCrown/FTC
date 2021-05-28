package net.forthecrown.emperor.useables.warps;

import net.forthecrown.emperor.utils.SilentPredicate;
import net.forthecrown.emperor.serialization.Deleteable;
import net.forthecrown.emperor.serialization.JsonSerializable;
import net.forthecrown.emperor.useables.Preconditionable;
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
