package net.forthecrown.events.custom;

import net.forthecrown.inventory.weapon.RoyalSword;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SwordRankUpEvent extends Event {
    private final int rankFrom, rankTo;
    private final RoyalSword sword;

    public SwordRankUpEvent(int rankFrom, int rankTo, RoyalSword sword) {
        this.rankFrom = rankFrom;
        this.rankTo = rankTo;
        this.sword = sword;
    }

    public int getRankFrom() {
        return rankFrom;
    }

    public int getRankTo() {
        return rankTo;
    }

    public RoyalSword getSword() {
        return sword;
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
