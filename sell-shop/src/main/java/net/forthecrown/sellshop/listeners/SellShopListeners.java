package net.forthecrown.sellshop.listeners;

import net.forthecrown.events.Events;
import net.forthecrown.sellshop.SellShopPlugin;

public class SellShopListeners {

  public static void registerAll(SellShopPlugin plugin) {
    Events.register(new AutoSellListener());
    Events.register(new ServerLoadListener(plugin));
    Events.register(new PlayerJoinListener());
  }
}
