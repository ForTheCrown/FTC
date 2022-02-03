package net.forthecrown.events.custom;

import net.forthecrown.dungeons.BossFightContext;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DungeonBossDeathEvent extends Event {
    private final BossFightContext context;
    private final DungeonBoss boss;

    public DungeonBossDeathEvent(BossFightContext context, DungeonBoss boss) {
        this.context = context;
        this.boss = boss;
    }

    public BossFightContext getContext() {
        return context;
    }

    public DungeonBoss getBoss() {
        return boss;
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
