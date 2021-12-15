package net.forthecrown.inventory.weapon;

import net.forthecrown.inventory.weapon.click.ClickHistory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WeaponUseContext extends WeaponContext {
    public final LivingEntity entity;
    public final EntityType type;
    public final double finalDamage;

    public WeaponUseContext(Player player, RoyalSword sword, LivingEntity entity, double finalDamage, ClickHistory history) {
        super(player, sword.getItem(), sword, history);
        this.entity = entity;
        this.type = entity.getType();
        this.finalDamage = finalDamage;
    }
}
