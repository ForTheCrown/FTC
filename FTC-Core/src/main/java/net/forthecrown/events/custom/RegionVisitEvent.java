package net.forthecrown.events.custom;

import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.CrownUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegionVisitEvent extends Event {
    private final CrownUser user;
    private final RegionPos originPos, destinationPos;
    private final PopulationRegion region;
    private final boolean hulkSmash;

    public RegionVisitEvent(CrownUser user, RegionPos originPos, RegionPos destinationPos, PopulationRegion region, boolean hulkSmash) {
        this.user = user;
        this.originPos = originPos;
        this.destinationPos = destinationPos;
        this.region = region;
        this.hulkSmash = hulkSmash;
    }

    public PopulationRegion getRegion() {
        return region;
    }

    public CrownUser getUser() {
        return user;
    }

    public RegionPos getDestinationPos() {
        return destinationPos;
    }

    public RegionPos getOriginPos() {
        return originPos;
    }

    public boolean originIsDestination() {
        return getOriginPos().equals(getDestinationPos());
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
