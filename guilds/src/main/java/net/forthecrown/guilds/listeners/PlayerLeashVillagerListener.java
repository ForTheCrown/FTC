package net.forthecrown.guilds.listeners;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerLeashVillagerListener implements Listener {

  private final Set<UUID> cancelFirst = new ObjectOpenHashSet<>();

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (event.getHand() != EquipmentSlot.HAND
        || !(event.getRightClicked() instanceof Villager villager)
        || villager.isLeashed()
    ) {
      return;
    }

    var player = event.getPlayer();
    var held = player.getInventory().getItemInMainHand();
    if (ItemStacks.isEmpty(held) || held.getType() != Material.LEAD) {
      return;
    }

    var guild = Guilds.getStandingInOwn(player);

    if (guild == null || !guild.getActiveEffects().contains(UnlockableChunkUpgrade.VILLAGERS)) {
      return;
    }

    villager.setLeashHolder(player);
    cancelFirst.add(villager.getUniqueId());

    Particle.VILLAGER_HAPPY.builder()
        .location(villager.getLocation().add(0, 2, 0))
        .count(5)
        .offset(0.2, 0.2, 0.2)
        .spawn();

    if (player.getGameMode() != GameMode.CREATIVE) {
      held.subtract(1);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityUnleash(EntityUnleashEvent event) {
    if (cancelFirst.remove(event.getEntity().getUniqueId())) {
      event.setCancelled(true);
    }
  }
}