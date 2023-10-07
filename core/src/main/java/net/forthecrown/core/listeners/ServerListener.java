package net.forthecrown.core.listeners;

import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.core.user.Components;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.enchantment.FtcEnchants;
import net.forthecrown.events.EarlyShutdownEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

class ServerListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
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

  @EventHandler(ignoreCancelled = true)
  public void onPluginDisable(PluginDisableEvent event) {
    Components.unregisterAll(event.getPlugin());
  }
}