package net.forthecrown.events.guilds;

import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class GuildSlimeSpawnListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onEntitySpawn(EntitySpawnEvent event) {
    var entity = event.getEntity();

    if (!(entity instanceof Slime)) {
      return;
    }

    var chunkOwner = GuildManager.get().getOwner(
        Guilds.getChunk(entity.getLocation())
    );

    if (chunkOwner == null
        || !chunkOwner.hasActiveEffect(UnlockableChunkUpgrade.NO_SLIME)
    ) {
      return;
    }

    event.setCancelled(true);
  }
}