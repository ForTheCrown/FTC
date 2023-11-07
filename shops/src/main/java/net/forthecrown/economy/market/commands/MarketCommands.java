package net.forthecrown.economy.market.commands;

import net.forthecrown.economy.market.MarketManager;

public class MarketCommands {

  static MarketArgument argument;

  public static void createCommands(MarketManager manager) {
    argument = new MarketArgument(manager);

    new CommandMarket(manager);
    new CommandMarketAppeal();
    new CommandMarketEditing();
    new CommandMarketWarning();
    new CommandMergeShop();
    new CommandShopTrust();
    new CommandTransferShop();
    new CommandUnclaimShop();
    new CommandUnmerge();
  }

}
