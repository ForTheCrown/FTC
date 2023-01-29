package net.forthecrown.events.player;

import static net.forthecrown.inventory.weapon.ability.WeaponAbility.UNLIMITED_USES;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import net.forthecrown.core.FTC;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.SwordConfig;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.inventory.weapon.ability.WeaponAbility;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
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
  public static final int USES_WARN_THRESHOLD = 10;

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

    onInteract(damager, event.getEntity(), null, true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    onInteract(event, event.getRightClicked(), null, false);
  }

  // Don't ignore cancelled, by default,
  // interactions with air blocks will be cancelled
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    onInteract(event, null, event.getClickedBlock(), event.getAction().isLeftClick());
  }

  private void onInteract(PlayerEvent event,
                          Entity entity,
                          Block block,
                          boolean leftClick
  ) {
    onInteract(event.getPlayer(), entity, block, leftClick);
  }

  private void onInteract(Player player,
                          Entity entity,
                          Block block,
                          boolean leftClick
  ) {
    if (!SwordAbilityManager.getInstance().isEnabled()
        && !FTC.inDebugMode()
    ) {
      return;
    }

    // Player has shield raised
    if (player.isBlocking()) {
      return;
    }

    var user = Users.get(player);
    var item = player.getInventory().getItemInMainHand();
    RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(item);

    if (sword == null) {
      return;
    }

    var ability = sword.getAbility();

    if (ability == null || player.hasCooldown(item.getType())) {
      return;
    }

    State abilityUsageAllowed
        = FtcFlags.query(player.getLocation(), FtcFlags.SWORD_UPGRADE_USE);

    if (abilityUsageAllowed == State.DENY) {
      return;
    }

    boolean usedSuccessfully = leftClick
        ? ability.onLeftClick(player, entity, block)
        : ability.onRightClick(player, entity, block);

    if (usedSuccessfully) {
      long cooldownTicks = ability.getCooldownTicks(sword.getRank());
      if (cooldownTicks > 0) {
        player.setCooldown(item.getType(), Math.toIntExact(cooldownTicks));
      }

      updateUses(ability, user, sword);
    }

    sword.update(item);
  }

  private void updateUses(WeaponAbility ability, User user, RoyalSword sword) {
    int remaining = ability.getRemainingUses();

    if (remaining == UNLIMITED_USES) {
      return;
    }

    int newRemaining = remaining - 1;

    ability.setRemainingUses(newRemaining);
    if (newRemaining <= 0) {
      sword.setAbility(null);

      user.sendMessage(
          Text.format("Upgrade {0} used up!",
              NamedTextColor.GRAY,
              ability.getType().fullDisplayName(user)
          )
      );
    } else if (remaining <= USES_WARN_THRESHOLD) {
      user.sendMessage(
          Text.format("Sword upgrade has &e{0, number} &ruses remaining.",
              NamedTextColor.GRAY,
              remaining
          )
      );
    }
  }
}