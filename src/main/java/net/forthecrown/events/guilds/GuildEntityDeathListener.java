package net.forthecrown.events.guilds;

import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class GuildEntityDeathListener implements Listener {


  @EventHandler(ignoreCancelled = true)
  public void onEntityDeath(EntityDeathEvent event) {
    var entity = event.getEntity();

    if (entity instanceof Player) {
      return;
    }

    var guild = GuildManager.get()
        .getOwner(Guilds.getChunk(entity.getLocation()));

    if (guild == null
        || !guild.hasActiveEffect(UnlockableChunkUpgrade.MORE_MOB_EXP)
    ) {
      return;
    }

    event.setDroppedExp((int) Math.ceil(event.getDroppedExp() * 1.5D));
  }
}