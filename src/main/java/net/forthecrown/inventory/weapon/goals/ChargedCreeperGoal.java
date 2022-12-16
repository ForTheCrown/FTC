package net.forthecrown.inventory.weapon.goals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class ChargedCreeperGoal implements WeaponKillGoal {
    private final int goal;

    @Override
    public boolean test(User user, Entity killed) {
        if (killed.getType() != EntityType.CREEPER) {
            return false;
        }

        Creeper creeper = (Creeper) killed;
        return creeper.isPowered();
    }

    @Override
    public Component loreDisplay() {
        return Component.text("Charged Creeper");
    }

    @Override
    public @NotNull String getName() {
        return "charged_creeper";
    }
}