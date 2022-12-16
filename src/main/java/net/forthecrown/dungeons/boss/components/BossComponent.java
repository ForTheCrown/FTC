package net.forthecrown.dungeons.boss.components;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.DungeonBoss;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * A component of a boss, allows for easily attaching
 * features to a boss
 * @param <T> The type of boss to add
 */
public interface BossComponent<T extends DungeonBoss> extends Listener {
    /**
     * A tick function, obviously gets called once per tick
     * @param boss The Boss currently fighting
     * @param context The battle's context
     */
    default void onTick(T boss, BossContext context) {}

    /**
     * Ran right after the boss is spawned
     * @param boss The boss that was spawned
     * @param context The fight's context
     */
    default void onSpawn(T boss, BossContext context) {}

    /**
     * Ran right after a boss is killed, at this point
     * the boss entity is still accessible
     *
     * @param boss The boss that died
     * @param context The fight's context
     * @param forced True, if the death was forced,
     *               like by a server restart or
     *               something similar
     */
    default void onDeath(T boss, BossContext context, boolean forced) {}

    /**
     * Called whenever the boss is hit,
     * depending on boss type, the
     * entity damage event may be null
     *
     * @param boss The boss that was hit
     * @param context The fight's context
     * @param event The damage event in
     *              which the boss was damaged
     */
    default void onHit(T boss, BossContext context, EntityDamageEvent event) {}
}
