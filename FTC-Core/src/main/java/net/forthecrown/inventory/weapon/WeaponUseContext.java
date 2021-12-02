package net.forthecrown.inventory.weapon;

import net.forthecrown.utils.Struct;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WeaponUseContext implements Struct {
    public final Player player;
    public final RoyalSword sword;
    public final ItemStack item;
    public final LivingEntity entity;
    public final EntityType type;
    public final double finalDamage;

    public WeaponUseContext(Player player, RoyalSword sword, LivingEntity entity, double finalDamage) {
        this.player = player;
        this.sword = sword;
        this.item = sword.getItem();
        this.entity = entity;
        this.type = entity.getType();
        this.finalDamage = finalDamage;
    }
}
