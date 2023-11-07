package net.forthecrown.economy.market.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;

public class CommandMarketEditing extends FtcCommand {

  public CommandMarketEditing() {
    super("MarketEditing");

    setAliases("toggleshopediting", "togglemarketediting");
    setPermission(EconPermissions.MARKETS);
    setDescription("Allows/disallows shop members to edit sign shops in your shop");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /MarketEditing
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      User user = getUserSender(c);

      if (!Markets.ownsShop(user)) {
        throw EconExceptions.NO_SHOP_OWNED;
      }

      MarketManager markets = ShopsPlugin.getPlugin().getMarkets();
      MarketShop shop = markets.get(user.getUniqueId());

      boolean state = !shop.isMemberEditingAllowed();
      shop.setMemberEditingAllowed(state);

      user.sendMessage(Messages.toggleMessage(EconMessages.MEMBER_EDIT_FORMAT, state));
      return 0;
    });
  }
}