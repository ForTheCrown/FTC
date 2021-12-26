package net.forthecrown.inventory.weapon;

import net.forthecrown.inventory.weapon.click.ClickHistory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WeaponUseContext extends WeaponContext {
    public final LivingEntity entity;
    public final EntityType type;
    private final double dmgDifference;
    public double baseDamage;

    public WeaponUseContext(Player player, RoyalSword sword, LivingEntity entity, double baseDamage, double finalDamage, ClickHistory history) {
        super(player, sword.getItem(), sword, history);
        this.entity = entity;
        this.type = entity.getType();
        this.baseDamage = baseDamage;
        this.dmgDifference = finalDamage - baseDamage;
    }

    public double getFinalDamage() {
        return baseDamage + dmgDifference;
    }
}
