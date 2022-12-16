package net.forthecrown.dungeons.boss;

import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A very basic and generic interface representing a
 * Dungeon Boss
 */
public interface DungeonBoss extends Listener {
    String getName();

    default Component name() {
        return Component.text(getName());
    }

    /**
     * Gets the check that anyone attempting to spawn the
     * boss must pass
     *
     * @return The spawning requirement, null if there's
     *         no requirement for spawning the boss
     */
    @Nullable SpawnTest getSpawnRequirement();

    /**
     * Gets whether the boss is alive or not
     * @return Isn't it obvious
     */
    boolean isAlive();

    /**
     * Gets all the components this boss has
     * @return Component set, null, if there are no components
     */
    @Nullable Set<BossComponent> getComponents();

    /**
     * Gets a specific component in this boss' component
     * list
     *
     * @param clazz The component's class
     * @param <T> The type of component
     * @return The component, null, if there are no components
     *         or if the component wasn't found
     */
    <T extends BossComponent> T getComponent(Class<T> clazz);

    /**
     * Spawns the boss, will not check for items, just spawns it
     */
    void spawn();

    /**
     * Kills the boss without forcing it
     */
    default void kill() {
        kill(false);
    }

    /**
     * Kills the boss
     * @param force Whether to force its death, true for
     *              stuff like server restarts
     */
    void kill(boolean force);

    /**
     * Gets the current battle's context
     * @return Current battle's context,
     *         null, if the boss isn't alive
     */
    BossContext currentContext();

    /**
     * Gets the spawn world of this boss
     * @return The world this boss is in
     */
    @NotNull World getWorld();

    /**
     * Gets the bounding box of the room
     * the boss is in
     * @return The boss' room's bounds
     */
    WorldBounds3i getRoom();

    /**
     * Gets the location the boss is meant to spawn
     * at
     * @return The boss' spawn location, cloned
     */
    Location getSpawn();

    default boolean attemptSpawn(Player player) {
        if (getSpawnRequirement() != null && !getSpawnRequirement().test(player)) {
            return false;
        }

        spawn();
        return true;
    }
}