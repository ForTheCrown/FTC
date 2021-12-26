package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.commands.emotes.EmoteSmooch;
import net.forthecrown.core.Permissions;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class DonatorWeaponGoal implements WeaponGoal {
    private final int rank;
    private final Key key;

    public DonatorWeaponGoal(int rank) {
        this.rank = rank;
        key = WeaponGoal.createKey(rank, "donator");
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
        return event.player.hasPermission(Permissions.DONATOR_1);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public Component loreDisplay() {
        return Component.text("Bought Tier-1 Donator ").append(EmoteSmooch.HEART);
    }
}
