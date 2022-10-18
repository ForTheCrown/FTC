package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.core.Vars;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * A RoyalSword's goal, which is achieved through killing entities.
 */
public interface WeaponGoal {

    /**
     * Gets the amount of kills needed to beat this goal
     * @return The goal's required kill amount
     */
    int getGoal();

    /**
     * Tests if the killed entity fits this goal
     * @param event The entity
     * @return Whether the entity is a legal kill for this goal or not
     */
    boolean test(EntityDamageByEntityEvent event);

    /**
     * Gets the amount to increment the goal
     * @param event The context of the weapon's usage
     * @return The amount to increase
     */
    default int getIncrementAmount(EntityDamageByEntityEvent event) {
        return Vars.swordGoalGainPerKill;
    }

    /**
     * The component to display in an item's lore to represent this goal
     * @return The goal's chat representation
     */
    Component loreDisplay();

    String getName();
}