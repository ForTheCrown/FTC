package net.forthecrown.commands.markets;

import net.forthecrown.commands.arguments.Arguments;
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
import net.forthecrown.user.Users;

import static net.forthecrown.core.Messages.STRUST_BLOCKED_SENDER;
import static net.forthecrown.core.Messages.STRUST_BLOCKED_TARGET;

public class CommandShopTrust extends FtcCommand {

    public CommandShopTrust() {
        super("shoptrust");

        setPermission(Permissions.MARKETS);
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
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.USER)
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");

                            if (user.equals(target)) {
                                throw Exceptions.format("Cannot trust self");
                            }

                            if (!Markets.ownsShop(user)) {
                                throw Exceptions.NO_SHOP_OWNED;
                            }

                            Users.testBlockedException(user, target,
                                    STRUST_BLOCKED_SENDER,
                                    STRUST_BLOCKED_TARGET
                            );

                            MarketManager region = Economy.get().getMarkets();
                            MarketShop shop = region.get(user.getUniqueId());

                            boolean trusted = shop.getMembers().contains(target.getUniqueId());

                            if (trusted) {
                                shop.untrust(target.getUniqueId());

                                user.sendMessage(Messages.shopUntrustSender(target));
                                target.sendMessage(Messages.shopUntrustTarget(user));
                            } else {
                                shop.trust(target.getUniqueId());

                                user.sendMessage(Messages.shopTrustSender(target));
                                target.sendMessage(Messages.shopTrustTarget(user));
                            }

                            return 0;
                        })
                );
    }
}