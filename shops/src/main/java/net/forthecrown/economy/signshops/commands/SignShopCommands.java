package net.forthecrown.economy.signshops.commands;

import net.forthecrown.economy.signshops.ShopManager;

public class SignShopCommands {

  public static void createCommands(ShopManager manager) {
    new CommandEditShop(manager);
    new CommandShopReselling(manager);
    new CommandShopHistory(manager);
  }
}
