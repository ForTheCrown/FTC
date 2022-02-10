package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.economy.houses.Houses;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class HouseReforgeGoal implements WeaponGoal {
    private final int rank;
    private final Key key;

    public HouseReforgeGoal(int rank) {
        this.rank = rank;
        this.key = WeaponGoal.createKey(rank, "house_reforge");
    }

    @Override
    public int getGoal() {
        return 1;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public boolean test(WeaponUseContext event) {
        return false;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public Component loreDisplay() {
        return Houses.ENABLED ?
                Component.text("Achieve a high reputation with a house to get the weapon upgraded") :
                Component.text("In progress, cannot upgrade further");
    }
}
