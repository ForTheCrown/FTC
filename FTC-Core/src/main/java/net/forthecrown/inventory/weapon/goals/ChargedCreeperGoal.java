package net.forthecrown.inventory.weapon.goals;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class ChargedCreeperGoal implements WeaponKillGoal {
    private final Key key;
    private final int rank, goal;

    public ChargedCreeperGoal(int goal, int rank) {
        this.rank = rank;
        this.goal = goal;
        this.key = WeaponGoal.createKey(rank, "charged_creeper");
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
        if (killed.getType() != EntityType.CREEPER) return false;
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
