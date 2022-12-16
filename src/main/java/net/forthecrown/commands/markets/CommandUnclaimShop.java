package net.forthecrown.commands.markets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.forthecrown.user.data.UserMarketData;

public class CommandUnclaimShop extends FtcCommand {

    public CommandUnclaimShop() {
        super("unclaimshop");

        setAliases("unclaimmarket");
        setDescription("Makes you unclaim your shop in Hazelguard");
        setPermission(Permissions.MARKETS);

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
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    check(user);

                    user.sendMessage(Messages.UNCLAIM_CONFIRM);
                    return 0;
                })

                .then(literal("confirm")
                        .executes(c -> {
                            User user = getUserSender(c);
                            check(user);

                            MarketManager markets = Economy.get().getMarkets();
                            MarketShop shop = markets.get(user.getUniqueId());

                            shop.unclaim(true);

                            user.sendMessage(Messages.MARKET_UNCLAIMED);
                            return 0;
                        })
                );
    }

    private void check(User user) throws CommandSyntaxException {
        UserMarketData ownership = user.getMarketData();

        if (!Markets.ownsShop(user)) {
            throw Exceptions.NO_SHOP_OWNED;
        }

        Markets.checkStatusChange(ownership);
    }
}