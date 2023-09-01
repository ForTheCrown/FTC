package net.forthecrown.guilds.listeners;

import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GuildEntityDeathListener implements Listener {


  @EventHandler(ignoreCancelled = true)
  public void onEntityDeath(PlayerDeathEvent event) {
    var player = event.getEntity();
    var guild = Guilds.getManager().getOwner(Guilds.getChunk(player.getLocation()));

    if (guild == null || !guild.hasActiveEffect(UnlockableChunkUpgrade.MORE_MOB_EXP)) {
      return;
    }

    event.setDroppedExp((int) Math.ceil(event.getDroppedExp() * 1.5D));
  }
}