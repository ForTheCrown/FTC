package net.forthecrown.antigrief.listeners;

import net.forthecrown.antigrief.Punishments;
import net.forthecrown.utils.PluginUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.slf4j.Logger;

public class ServerLoadListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onServerLoad(ServerLoadEvent event) {
    Logger logger = PluginUtil.getPlugin().getSLF4JLogger();

    Punishments.get().getPostStartup().forEach(runnable -> {
      try {
        runnable.run();
      } catch (Throwable t) {
        logger.error("Error running post startup task", t);
      }
    });
  }
}
