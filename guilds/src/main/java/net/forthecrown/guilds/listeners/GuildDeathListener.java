package net.forthecrown.guilds.listeners;

import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GuildDeathListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerDeath(PlayerDeathEvent event) {
    var player = event.getPlayer();
    var guild = Guilds.getStandingInOwn(player);

    if (guild == null) {
      return;
    }

    var effects = guild.getActiveEffects();

    if (effects.contains(UnlockableChunkUpgrade.KEEPINV)) {
      event.setKeepInventory(true);
      event.getDrops().clear();
    }

    if (effects.contains(UnlockableChunkUpgrade.KEEPEXP)) {
      event.setShouldDropExperience(false);
      event.setKeepLevel(true);
    }
  }
}