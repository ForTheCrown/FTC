package net.forthecrown.inventory.weapon;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    static WeaponGoal dungeonBoss(DungeonBoss<?> boss, int goal, int rank) {
        return new DungeonBossGoal(boss, goal, rank);
    }

    static WeaponGoal damage(int goal, int rank) {
        return new DamageGoal(rank, goal);
    }

    interface WeaponKillGoal extends WeaponGoal {
        boolean test(Entity entity);

        @Override
        default boolean test(WeaponUseContext event) {
            if(event.entity.getHealth() - event.finalDamage > 0) return false;

            return test(event.entity);
        }
    }

    class ChargedCreeperGoal implements WeaponKillGoal {
        private final Key key;
        private final int rank, goal;

        public ChargedCreeperGoal(int goal, int rank) {
            this.rank = rank;
            this.goal = goal;
            this.key = Keys.ftc("goal_charged_creeper");
        }

        @Override
        public int getGoal() {
            return goal;
        }

        @Override
        public int getRank() {
            return rank;
        }

        @Override
        public boolean test(Entity killed) {
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

    class SimpleGoal implements WeaponKillGoal {
        private final int goal, rank;
        private final EntityType type;
        private final Key key;

        public SimpleGoal(int goal, int rank, @Nullable EntityType type) {
            this.goal = goal;
            this.type = type;
            this.rank = rank;
            this.key = Keys.ftc("goal_" + (type == null ? "any" : type.name().toLowerCase()));
        }

        @Override
        public int getGoal() {
            return goal;
        }

        @Override
        public int getRank() {
            return rank;
        }

        @Override
        public boolean test(Entity killed) {
            return type == null || killed.getType() == type;
        }

        @Override
        public @NotNull Key key() {
            return key;
        }

        @Override
        public Component loreDisplay() {
            return type == null ? Component.text("Any entity") : Component.translatable(Bukkit.getUnsafe().getTranslationKey(type));
        }
    }

    class EndBossGoal implements WeaponKillGoal {
        private final int goal, rank;
        private final Key key;
        private final EntityType type;

        public EndBossGoal(EntityType type, int goal, int rank) {
            this.goal = goal;
            this.rank = rank;
            this.type = type;

            this.key = Keys.ftc("goal_boss_" + type.name().toLowerCase() + '_' + rank);
        }

        @Override
        public int getGoal() {
            return goal;
        }

        @Override
        public int getRank() {
            return rank;
        }

        @Override
        public boolean test(Entity killed) {
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

    class DamageGoal implements WeaponGoal {
        private final Key key;
        private final int rank, goal;

        public DamageGoal(int rank, int goal) {
            this.rank = rank;
            this.goal = goal;

            this.key = Keys.ftc("goal_damage_" + rank);
        }

        @Override
        public int getGoal() {
            return goal;
        }

        @Override
        public int getRank() {
            return rank;
        }

        @Override
        public boolean test(WeaponUseContext event) {
            return true;
        }

        @Override
        public int getIncrementAmount(WeaponUseContext event) {
            return (int) event.finalDamage;
        }

        @Override
        public @NotNull Key key() {
            return key;
        }

        @Override
        public Component loreDisplay() {
            return Component.text("Dealt damage");
        }
    }

    class DungeonBossGoal implements WeaponKillGoal {
        private final Key key;
        private final int rank, goal;
        private final DungeonBoss<?> boss;

        public DungeonBossGoal(DungeonBoss<?> boss, int goal, int rank) {
            this.rank = rank;
            this.goal = goal;
            this.boss = boss;

            this.key = Keys.ftc("goal_kill_" + boss.getName().toLowerCase().replaceAll(" ", "_"));
        }
        @Override
        public int getGoal() { return goal; }

        @Override
        public int getRank() { return rank; }

        @Override
        public @NotNull Key key() { return key; }

        @Override
        public Component loreDisplay() { return Component.text("Kill " + boss.getName()); }

        @Override
        public boolean test(Entity entity) {
            return entity.getPersistentDataContainer().has(Bosses.BOSS_TAG, PersistentDataType.BYTE)
                    && entity.getType() == boss.getBossEntity().getType();
        }
    }
}
