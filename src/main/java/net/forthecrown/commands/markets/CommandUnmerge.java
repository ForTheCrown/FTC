package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandUnmerge extends FtcCommand {

  public CommandUnmerge() {
    super("unmerge");

    setDescription("Unmerges the shop you own with the shop it's merged with");
    setPermission(Permissions.MARKETS);
    setAliases("marketunmerge", "shopunmerge", "unmergeshop", "unmergemarket");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /unmerge
   *
   * Permissions used:
   * ftc.markets
   *
   * Main Author: Julie
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);

          if (!Markets.ownsShop(user)) {
            throw Exceptions.NO_SHOP_OWNED;
          }

          MarketManager markets = Economy.get().getMarkets();
          MarketShop shop = markets.get(user.getUniqueId());

          if (!shop.isMerged()) {
            throw Exceptions.NOT_MERGED;
          }

          User target = shop.getMerged().ownerUser();
          target.unloadIfOffline();

          shop.unmerge();

          user.sendMessage(Messages.marketUnmergeSender(target));
          target.sendOrMail(Messages.marketUnmergeTarget(user));
          return 0;
        });
  }
}