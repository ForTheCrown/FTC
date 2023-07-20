package net.forthecrown.core.listeners;

import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.enchantment.FtcEnchants;
import net.forthecrown.events.EarlyShutdownEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

class ServerLoadListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    CorePlugin plugin = JavaPlugin.getPlugin(CorePlugin.class);
    plugin.getUserService().onServerLoaded();

    FtcHelpList helpList = FtcHelpList.helpList();
    helpList.update();

    FtcEnchants.closeRegistrations();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEarlyShutdown(EarlyShutdownEvent event) {
    CorePlugin plugin = JavaPlugin.getPlugin(CorePlugin.class);
    UserServiceImpl service = plugin.getUserService();
    service.shutdown();
  }
}