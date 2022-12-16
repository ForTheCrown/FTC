package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.user.User;

public class CommandMarketEditing extends FtcCommand {

    public CommandMarketEditing() {
        super("MarketEditing");

        setAliases("toggleshopediting", "togglemarketediting");
        setPermission(Permissions.MARKETS);
        setDescription("Allows/disallows shop member to edit sign shops");

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
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            User user = getUserSender(c);

            if (!Markets.ownsShop(user)) {
                throw Exceptions.NO_SHOP_OWNED;
            }

            MarketManager markets = Economy.get().getMarkets();
            MarketShop shop = markets.get(user.getUniqueId());

            boolean state = !shop.isMemberEditingAllowed();
            shop.setMemberEditingAllowed(state);

            user.sendMessage(
                    Messages.toggleMessage(
                            Messages.MEMBER_EDIT_FORMAT,
                            state
                    )
            );
            return 0;
        });
    }
}