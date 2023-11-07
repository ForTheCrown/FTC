package net.forthecrown.sellshop.listeners;

import net.forthecrown.sellshop.SellShopPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

class ServerLoadListener implements Listener {

  private final SellShopPlugin plugin;

  public ServerLoadListener(SellShopPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    plugin.getSellShop().load();
  }
}
