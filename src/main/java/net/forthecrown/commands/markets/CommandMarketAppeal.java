package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Vars;
import net.forthecrown.economy.market.MarketScan;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;

import static net.forthecrown.economy.market.MarketEviction.SOURCE_AUTOMATIC;

public class CommandMarketAppeal extends FtcCommand {

    public CommandMarketAppeal() {
        super("MarketAppeal");

        setPermission(Permissions.MARKETS);
        setDescription("Appeals an automated market eviction");

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
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);

                    if (!MarketManager.ownsShop(user)) {
                        throw Exceptions.NO_SHOP_OWNED;
                    }

                    MarketManager markets = Crown.getEconomy().getMarkets();
                    MarketShop shop = markets.get(user.getUniqueId());

                    if (!shop.markedForEviction()) {
                        throw Exceptions.NOT_MARKED_EVICTION;
                    }

                    var eviction = shop.getEviction();

                    if (!eviction.getSource().equals(SOURCE_AUTOMATIC)) {
                        throw Exceptions.NON_AUTO_APPEAL;
                    }

                    MarketScan scan = MarketScan.create(markets.getWorld(), shop);
                    int total = scan.stockedCount() + scan.unstockedCount();
                    float required = Vars.markets_minStockRequired * total;

                    if (total < Vars.markets_minShopAmount) {
                        user.sendMessage(Messages.cannotAppeal(Messages.tooLittleShops()));
                        return 0;
                    }

                    if (scan.stockedCount() < required) {
                        user.sendMessage(Messages.cannotAppeal(Messages.MARKET_EVICT_STOCK));
                        return 0;
                    }

                    shop.stopEviction();
                    user.sendMessage(Messages. MARKET_APPEALED_EVICTION);

                    return 0;
                });
    }
}