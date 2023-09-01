package net.forthecrown.core.listeners;

import net.forthecrown.Loggers;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.enchantment.FtcEnchants;
import net.forthecrown.events.EarlyShutdownEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

class ServerListener implements Listener {

  private static final Logger LOGGER = Loggers.getLogger();

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    LOGGER.debug("SERVER LOAD CALLED ------------------------------------------------------------");

    CorePlugin plugin = JavaPlugin.getPlugin(CorePlugin.class);
    plugin.getUserService().onServerLoaded();

    FtcEnchants.closeRegistrations();

    FtcHelpList helpList = FtcHelpList.helpList();
    helpList.update();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEarlyShutdown(EarlyShutdownEvent event) {
    CorePlugin plugin = JavaPlugin.getPlugin(CorePlugin.class);
    UserServiceImpl service = plugin.getUserService();
    service.shutdown();
  }
}