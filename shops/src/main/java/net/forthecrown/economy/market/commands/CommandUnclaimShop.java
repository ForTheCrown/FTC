package net.forthecrown.economy.market.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandUnclaimShop extends FtcCommand {

  public CommandUnclaimShop() {
    super("unclaimshop");

    setAliases("unclaimmarket");
    setDescription("Makes you unclaim your shop in Hazelguard");
    setPermission(EconPermissions.MARKETS);
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /unclaimshop
   *
   * Permissions used:
   * ftc.markets
   *
   * Main Author: Julie <3
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User user = getUserSender(c);
          check(user);

          user.sendMessage(EconMessages.UNCLAIM_CONFIRM);
          return 0;
        })

        .then(literal("confirm")
            .executes(c -> {
              User user = getUserSender(c);
              check(user);

              MarketManager markets = ShopsPlugin.getPlugin().getMarkets();
              MarketShop shop = markets.get(user.getUniqueId());

              shop.unclaim(true);

              user.sendMessage(EconMessages.MARKET_UNCLAIMED);
              return 0;
            })
        );
  }

  private void check(User user) throws CommandSyntaxException {
    if (!Markets.ownsShop(user)) {
      throw EconExceptions.NO_SHOP_OWNED;
    }

    Markets.checkStatusChange(user);
  }
}