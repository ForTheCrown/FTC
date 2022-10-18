package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface WeaponKillGoal extends WeaponGoal {
    boolean test(User user, Entity entity);

    @Override
    default boolean test(EntityDamageByEntityEvent context) {
        LivingEntity living = (LivingEntity) context.getEntity();

        if (living.getHealth() - context.getFinalDamage() > 0) {
            return false;
        }

        return test(Users.get(context.getDamager().getUniqueId()), context.getEntity());
    }
}