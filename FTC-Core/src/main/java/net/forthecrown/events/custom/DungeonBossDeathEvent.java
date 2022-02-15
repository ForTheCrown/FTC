package net.forthecrown.events.custom;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.DungeonBoss;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DungeonBossDeathEvent extends Event {
    private final BossContext context;
    private final DungeonBoss boss;

    public DungeonBossDeathEvent(BossContext context, DungeonBoss boss) {
        this.context = context;
        this.boss = boss;
    }

    public BossContext getContext() {
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
