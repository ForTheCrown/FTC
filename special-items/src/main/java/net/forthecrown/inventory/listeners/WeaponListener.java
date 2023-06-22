package net.forthecrown.inventory.listeners;

import static net.forthecrown.inventory.weapon.ability.WeaponAbility.UNLIMITED_USES;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import net.forthecrown.Loggers;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.ItemsPlugin;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.SwordConfig;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.inventory.weapon.ability.WeaponAbility;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
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
import org.slf4j.Logger;

public class WeaponListener implements Listener {
  public static final int USES_WARN_THRESHOLD = 10;

  private static final Logger LOGGER = Loggers.getLogger();

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
    if (!SwordAbilityManager.getInstance().isEnabled() && !LOGGER.isDebugEnabled()) {
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

    var location = user.getLocation();
    State abilityUsageAllowed = WorldGuard.getInstance()
        .getPlatform()
        .getRegionContainer()
        .createQuery()
        .queryState(
            BukkitAdapter.adapt(location),
            WorldGuardPlugin.inst().wrapPlayer(user.getPlayer()),
            ItemsPlugin.SWORD_USE_ALLOWED
        );

    if (abilityUsageAllowed == State.DENY) {
      return;
    }

    boolean usedSuccessfully = leftClick
        ? ability.onLeftClick(player, sword, entity, block)
        : ability.onRightClick(player, sword, entity, block);

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
    SwordAbilityManager.getInstance()
        .getRegistry()
        .getHolderByValue(ability.getType())

        .ifPresent(holder -> {
          sword.onAbilityUse(holder);
          int totalUses = sword.getTotalUses(holder);
          int newLevel = holder.getValue().getLevel(totalUses);

          if (ability.getLevel() >= newLevel) {
            return;
          }

          ability.setLevel(newLevel);

          user.sendMessage(Text.format(
              "Sword upgrade &e{0}&r upgraded to &eLevel {1, number, -roman}&r.",
              NamedTextColor.GRAY,
              holder.getValue().fullDisplayName(user),
              newLevel
          ));
        });

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