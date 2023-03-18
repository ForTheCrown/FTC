package net.forthecrown.commands.markets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
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
import net.forthecrown.user.UserManager;

public class CommandTransferShop extends FtcCommand {

  public CommandTransferShop() {
    super("transfershop");

    setPermission(Permissions.MARKETS);
    setAliases("transfermarket", "shoptransfer", "markettransfer");
    setDescription("Transfers a player's shop in Hazelguard to another player");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /transfershop <user>
   * /transfershop <user> <confirm>
   *
   * Permissions used:
   * ftc.markets
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>")
        .addInfo("Transfers the shop you own to a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              check(user, target);

              user.sendMessage(Messages.marketTransferConfirm(target));
              return 0;
            })

            .then(literal("confirm")
                .executes(c -> {
                  User user = getUserSender(c);
                  User target = Arguments.getUser(c, "user");

                  check(user, target);

                  MarketManager markets = Economy.get().getMarkets();
                  MarketShop shop = markets.get(user.getUniqueId());

                  shop.transfer(target);

                  user.sendMessage(Messages.marketTransferredSender(target));
                  target.sendOrMail(Messages.marketTransferredTarget(user));
                  return 0;
                })
            )
        );
  }

  private void check(User user, User target) throws CommandSyntaxException {
    if (user.equals(target)) {
      throw Exceptions.TRANSFER_SELF;
    }

    if (!Markets.ownsShop(user)) {
      throw Exceptions.NO_SHOP_OWNED;
    }

    if (Markets.ownsShop(target)) {
      throw Exceptions.marketTargetHasShop(target);
    }

    Markets.checkStatusChange(user.getMarketData());

    if (!Markets.canChangeStatus(target)) {
      throw Exceptions.marketTargetStatus(target);
    }

    var alts = UserManager.get().getAlts();
    if (alts.isAlt(target.getUniqueId())) {
      throw Exceptions.ALTS_CANNOT_OWN;
    }
  }
}