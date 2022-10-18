package net.forthecrown.commands.markets;

import net.forthecrown.text.Messages;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;

public class CommandUnmerge extends FtcCommand {

    public CommandUnmerge() {
        super("unmerge");

        setDescription("Unmerges the shop you own with the shops it's merged with");
        setPermission(Permissions.MARKETS);
        setAliases("marketunmerge", "shopunmerge", "unmergeshop", "unmergemarket");

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
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);

                    if (!MarketManager.ownsShop(user)) {
                        throw Exceptions.NO_SHOP_OWNED;
                    }

                    MarketManager markets = Crown.getEconomy().getMarkets();
                    MarketShop shop = markets.get(user.getUniqueId());

                    if(!shop.isMerged()) {
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