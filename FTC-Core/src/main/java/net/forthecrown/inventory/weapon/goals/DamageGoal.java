package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class DamageGoal implements WeaponGoal {
    private final Key key;
    private final int rank, goal;

    public DamageGoal(int rank, int goal) {
        this.rank = rank;
        this.goal = goal;

        this.key = WeaponGoal.createKey(rank, "damage");
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
        return (int) event.getFinalDamage();
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
