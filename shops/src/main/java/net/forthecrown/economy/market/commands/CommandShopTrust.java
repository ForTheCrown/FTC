package net.forthecrown.economy.market.commands;

import static net.forthecrown.economy.EconMessages.STRUST_BLOCKED_SENDER;
import static net.forthecrown.economy.EconMessages.STRUST_BLOCKED_TARGET;

import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;

public class CommandShopTrust extends FtcCommand {

  public CommandShopTrust() {
    super("shoptrust");

    setPermission(EconPermissions.MARKETS);
    setAliases("shopuntrust", "markettrust", "marketuntrust");
    setDescription("Trusts/untrusts a player in your shop");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /shoptrust <user>
   *
   * Permissions used:
   * ftc.markets
   *
   * Main Author: Julie :D
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>")
        .addInfo("Trusts/untrusts a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              if (user.equals(target)) {
                throw Exceptions.format("Cannot trust self");
              }

              if (!Markets.ownsShop(user)) {
                throw EconExceptions.NO_SHOP_OWNED;
              }

              MarketManager region = ShopsPlugin.getPlugin().getMarkets();
              MarketShop shop = region.get(user.getUniqueId());

              boolean trusted = shop.getMembers().contains(target.getUniqueId());

              if (trusted) {
                shop.untrust(target.getUniqueId());

                user.sendMessage(EconMessages.shopUntrustSender(target));
                target.sendMessage(EconMessages.shopUntrustTarget(user));
              } else {
                UserBlockList.testBlockedException(user, target,
                    STRUST_BLOCKED_SENDER,
                    STRUST_BLOCKED_TARGET
                );

                shop.trust(target.getUniqueId());

                user.sendMessage(EconMessages.shopTrustSender(target));
                target.sendMessage(EconMessages.shopTrustTarget(user));
              }

              return 0;
            })
        );
  }
}