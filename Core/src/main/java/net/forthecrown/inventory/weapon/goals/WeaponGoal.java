package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.core.Crown;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface WeaponGoal extends Keyed {
    int getKillGoal();
    boolean isValidTarget(Entity killed);

    @Override
    @NotNull Key key();

    static WeaponGoal simple(int goal, EntityType type) {
        return new SimpleWeaponGoal(goal, type);
    }

    class SimpleWeaponGoal implements WeaponGoal {
        private final int goal, entOrdinal;
        private final Key key;

        public SimpleWeaponGoal(int goal, EntityType type) {
            this.goal = goal;
            this.entOrdinal = type.ordinal();
            this.key = Crown.coreKey("goal_" + type.name().toLowerCase());
        }

        @Override
        public int getKillGoal() {
            return goal;
        }

        @Override
        public boolean isValidTarget(Entity killed) {
            return killed.getType().ordinal() == entOrdinal;
        }

        @Override
        public @NotNull Key key() {
            return key;
        }
    }
}
