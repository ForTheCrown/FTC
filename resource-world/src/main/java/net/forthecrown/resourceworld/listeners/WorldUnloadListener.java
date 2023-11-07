package net.forthecrown.resourceworld.listeners;

import net.forthecrown.FtcServer;
import net.forthecrown.Loggers;
import net.forthecrown.Worlds;
import net.forthecrown.resourceworld.RwPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.slf4j.Logger;

public class WorldUnloadListener implements Listener {

  private static final Logger LOGGER = Loggers.getPluginLogger();

  private final RwPlugin plugin;

  public WorldUnloadListener(RwPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onWorldUnload(WorldUnloadEvent event) {
    if (!event.getWorld().getName().equals(Worlds.RESOURCE_NAME)) {
      return;
    }

    var rw = plugin.getResourceWorld();
    var tracker = plugin.getTracker();
    var config = plugin.getRwConfig();

    rw.setGatesOpen(false);
    tracker.reset();

    // Attempt to announce closing
    if (config.messages.resetStart == null) {
      LOGGER.warn("resetStart message is null, cannot announce");
    } else {
      FtcServer.server().announce(config.messages.resetStart);
    }
  }
}
