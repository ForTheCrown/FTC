package net.forthecrown.economy.market.commands;

import static net.forthecrown.economy.market.MarketEviction.SOURCE_AUTOMATIC;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketScan;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandMarketAppeal extends FtcCommand {

  public CommandMarketAppeal() {
    super("MarketAppeal");

    setPermission(EconPermissions.MARKETS);
    setDescription("Appeals an automated market eviction");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /MarketAppeal
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);

          if (!Markets.ownsShop(user)) {
            throw EconExceptions.NO_SHOP_OWNED;
          }

          var plugin = ShopsPlugin.getPlugin();
          var config = plugin.getShopConfig();
          MarketManager markets = plugin.getMarkets();

          MarketShop shop = markets.get(user.getUniqueId());

          if (!shop.markedForEviction()) {
            throw EconExceptions.NOT_MARKED_EVICTION;
          }

          var eviction = shop.getEviction();

          if (!eviction.getSource().equals(SOURCE_AUTOMATIC)) {
            throw EconExceptions.NON_AUTO_APPEAL;
          }

          MarketScan scan = MarketScan.create(Markets.getWorld(), shop, plugin.getShops());
          int total = scan.stockedCount() + scan.unstockedCount();
          float required = config.getMinStock() * total;

          if (total < config.getMinimumShopAmount()) {
            user.sendMessage(EconMessages.cannotAppeal(EconMessages.tooLittleShops()));
            return 0;
          }

          if (scan.stockedCount() < required) {
            user.sendMessage(EconMessages.cannotAppeal(EconMessages.MARKET_EVICT_STOCK));
            return 0;
          }

          shop.stopEviction();
          user.sendMessage(EconMessages.MARKET_APPEALED_EVICTION);

          return 0;
        });
  }
}