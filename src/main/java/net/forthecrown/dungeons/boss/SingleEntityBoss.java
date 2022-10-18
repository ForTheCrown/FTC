package net.forthecrown.dungeons.boss;

import org.bukkit.entity.Mob;
import org.jetbrains.annotations.Nullable;

/**
 * A boss with a single entity
 */
public interface SingleEntityBoss extends DungeonBoss {
    /**
     * Gets the boss' entity
     * @return The boss entity, null, if the boss isn't alive
     */
    @Nullable Mob getBossEntity();
}
