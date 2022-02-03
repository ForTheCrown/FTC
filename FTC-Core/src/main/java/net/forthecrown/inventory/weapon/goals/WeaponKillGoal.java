package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.entity.Entity;

public interface WeaponKillGoal extends WeaponGoal {
    boolean test(CrownUser user, Entity entity);

    @Override
    default boolean test(WeaponUseContext context) {
        if (context.entity.getHealth() - context.getFinalDamage() > 0) return false;

        return test(UserManager.getUser(context.player), context.entity);
    }
}
