package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * A RoyalSword's goal, which is achieved through killing entities.
 */
public interface WeaponGoal extends Keyed {

    /**
     * Gets the amount of kills needed to beat this goal
     * @return The goal's required kill amount
     */
    int getGoal();

    /**
     * Gets the rank at which a sword has to beat this goal
     * @return The rank at which swords have to beat this
     */
    int getRank();

    /**
     * Tests if the killed entity fits this goal
     * @param event The entity
     * @return Whether the entity is a legal kill for this goal or not
     */
    boolean test(WeaponUseContext event);

    /**
     * Gets the amount to increment the goal
     * @param event The context of the weapon's usage
     * @return The amount to increase
     */
    default int getIncrementAmount(WeaponUseContext event) {
        return ComVars.swordGoalGainPerKill();
    }

    @Override
    @NotNull Key key();

    /**
     * The component to display in an item's lore to represent this goal
     * @return The goal's chat representation
     */
    Component loreDisplay();

    /**
     * Creates a simple weapon goal
     * @param type The type of the entity needed to kill
     * @param goal The goal
     * @param rank The rank
     * @return The created weapon goal
     */
    static WeaponGoal simple(EntityType type, int goal, int rank) {
        return new SimpleGoal(goal, rank, type);
    }

    static Key createKey(int rank, String value) {
        return Keys.forthecrown("goal_r" + rank + "_" + value);
    }

    /**
     * Creates a weapon goal that allows any entity
     * @param goal The kill goal
     * @param rank The rank
     * @return The created weapon goal
     */
    static WeaponGoal anyEntity(int goal, int rank) {
        return new SimpleGoal(goal, rank, null);
    }

    static WeaponGoal endBoss(EntityType type, int goal, int rank) {
        return new EndBossGoal(type, goal, rank);
    }

    static WeaponGoal dungeonBoss(KeyedBoss boss, int goal, int rank) {
        return new DungeonBossGoal(boss, goal, rank);
    }

    static WeaponGoal damage(int goal, int rank) {
        return new DamageGoal(rank, goal);
    }

}
