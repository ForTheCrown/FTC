package net.forthecrown.sellshop.listeners;

import net.forthecrown.events.Events;

public class SellShopListeners {

  public static void registerAll() {
    Events.register(new AutoSellListener());
  }
}
