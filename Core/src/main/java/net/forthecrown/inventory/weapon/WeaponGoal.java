package net.forthecrown.inventory.weapon;

import net.forthecrown.core.Crown;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
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
    int getKillGoal();

    /**
     * Gets the rank at which a sword has to beat this goal
     * @return The rank at which swords have to beat this
     */
    int getGoalRank();

    /**
     * Tests if the killed entity fits this goal
     * @param killed The entity
     * @return Whether the entity is a legal kill for this goal or not
     */
    boolean isValidTarget(Entity killed);

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

    class ChargedCreeperGoal implements WeaponGoal {
        private final Key key;
        private final int rank, goal;

        public ChargedCreeperGoal(int goal, int rank) {
            this.rank = rank;
            this.goal = goal;
            this.key = Crown.coreKey("goal_charged_creeper");
        }

        @Override
        public int getKillGoal() {
            return goal;
        }

        @Override
        public int getGoalRank() {
            return rank;
        }

        @Override
        public boolean isValidTarget(Entity killed) {
            if(killed.getType() != EntityType.CREEPER) return false;
            Creeper creeper = (Creeper) killed;

            return creeper.isPowered();
        }

        @Override
        public @NotNull Key key() {
            return key;
        }

        @Override
        public Component loreDisplay() {
            return Component.text("Charged Creeper");
        }
    }

    class SimpleGoal implements WeaponGoal {
        private final int goal, rank;
        private final EntityType type;
        private final Key key;

        public SimpleGoal(int goal, int rank, EntityType type) {
            this.goal = goal;
            this.type = type;
            this.rank = rank;
            this.key = Crown.coreKey("goal_" + type.name().toLowerCase());
        }

        @Override
        public int getKillGoal() {
            return goal;
        }

        @Override
        public int getGoalRank() {
            return rank;
        }

        @Override
        public boolean isValidTarget(Entity killed) {
            return killed.getType() == type;
        }

        @Override
        public @NotNull Key key() {
            return key;
        }

        @Override
        public Component loreDisplay() {
            return Component.translatable(Bukkit.getUnsafe().getTranslationKey(type));
        }
    }
}
