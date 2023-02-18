package net.forthecrown.events.guilds;

import java.util.Arrays;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class GuildDurabilityListener implements Listener {


  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerItemDamage(PlayerItemDamageEvent event) {
    var player = event.getPlayer();

    if (!GuildMoveListener.isInOwnGuild(player.getUniqueId())) {
      return;
    }

    var item = event.getItem();
    boolean armorItem = Arrays.stream(player.getInventory().getArmorContents())
        .filter(ItemStacks::notEmpty)
        .anyMatch(itemStack -> itemStack.equals(item));

    var guild = Users.get(player).getGuild();

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
    if (Util.RANDOM.nextBoolean()) {
      cancellable.setCancelled(true);
    }
  }
}