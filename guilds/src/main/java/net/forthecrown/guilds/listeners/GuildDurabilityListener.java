package net.forthecrown.guilds.listeners;

import java.util.Arrays;
import java.util.Random;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class GuildDurabilityListener implements Listener {

  private final Random random = new Random();

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerItemDamage(PlayerItemDamageEvent event) {
    var player = event.getPlayer();
    var guild = Guilds.getStandingInOwn(player);

    if (guild == null) {
      return;
    }

    var item = event.getItem();
    boolean armorItem = Arrays.stream(player.getInventory().getArmorContents())
        .filter(ItemStacks::notEmpty)
        .anyMatch(itemStack -> itemStack.equals(item));

    if (armorItem) {
      if (!guild.hasActiveEffect(UnlockableChunkUpgrade.STRONG_ARMOR)) {
        return;
      }
    } else if (!guild.hasActiveEffect(UnlockableChunkUpgrade.STRONG_TOOLS)) {
      return;
    }

    randomCancel(event);
  }

  private void randomCancel(Cancellable cancellable) {
    if (random.nextBoolean()) {
      cancellable.setCancelled(true);
    }
  }
}