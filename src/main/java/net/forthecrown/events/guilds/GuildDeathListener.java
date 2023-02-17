package net.forthecrown.events.guilds;

import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.user.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GuildDeathListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerDeath(PlayerDeathEvent event) {
    var player = event.getPlayer();

    if (!GuildMoveListener.isInOwnGuild(player.getUniqueId())) {
      return;
    }

    var guild = Users.get(player).getGuild();
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