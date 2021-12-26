package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.inventory.weapon.WeaponUseContext;
import org.bukkit.entity.Entity;

public interface WeaponKillGoal extends WeaponGoal {
    boolean test(Entity entity);

    @Override
    default boolean test(WeaponUseContext context) {
        if (context.entity.getHealth() - context.getFinalDamage() > 0) return false;

        return test(context.entity);
    }
}
