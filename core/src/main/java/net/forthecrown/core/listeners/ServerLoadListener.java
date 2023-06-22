package net.forthecrown.core.listeners;

import net.forthecrown.core.CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerLoadListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    CorePlugin plugin = JavaPlugin.getPlugin(CorePlugin.class);
    plugin.getUserService().freezeRegistries();
  }
}