package net.forthecrown.economy.signshops.listeners;

import net.forthecrown.economy.signshops.ShopManager;
import net.forthecrown.events.Events;

public class ShopListeners {

  public static void registerAll(ShopManager manager) {
    Events.register(new ShopCreateListener(manager));
    Events.register(new ShopDestroyListener(manager));
    Events.register(new ShopInteractionListener(manager));
    Events.register(new ShopInventoryListener());
  }
}
