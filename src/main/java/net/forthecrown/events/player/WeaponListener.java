package net.forthecrown.events.player;

import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class WeaponListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)
                || !(event.getEntity() instanceof LivingEntity)
                || event.getEntity() instanceof ArmorStand
        ) {
            return;
        }

        var item = damager.getInventory().getItemInMainHand();
        RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(item);

        if (sword == null) {
            return;
        }

        if (GeneralConfig.allowNonOwnerSwords
                || sword.getOwner().equals(damager.getUniqueId())
        ) {
            sword.damage(damager, event, item);
        }
    }
}