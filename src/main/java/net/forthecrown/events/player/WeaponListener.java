package net.forthecrown.events.player;

import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.SwordConfig;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    if (SwordConfig.allowNonOwnerSwords
        || sword.getOwner().equals(damager.getUniqueId())
    ) {
      sword.damage(damager, event, item);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    onInteract(event, event.getRightClicked(), false);
  }

  // Don't ignore cancelled, by default,
  // interactions with air blocks will be cancelled
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    onInteract(event, null, event.getAction().isLeftClick());
  }

  private void onInteract(PlayerEvent event, Entity entity, boolean leftClick) {
    var player = event.getPlayer();
    var item = player.getInventory().getItemInMainHand();
    RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(item);

    if (sword == null) {
      return;
    }

    var ability = sword.getAbility();

    if (ability == null || player.hasCooldown(item.getType())) {
      return;
    }

    boolean giveCooldown = leftClick
        ? ability.onLeftClick(player, entity)
        : ability.onRightClick(player, entity);

    int cooldownTicks = ability.getCooldownTicks();
    if (cooldownTicks > 0 && giveCooldown) {
      player.setCooldown(item.getType(), cooldownTicks);
    }
  }
}