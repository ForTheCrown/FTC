package net.forthecrown.commands.markets;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.MarketOwnership;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
                .then(argument("user", UserArgument.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            UserInteractions inter = user.getInteractions();
                            MarketOwnership ownership = user.getMarketOwnership();

                            CrownUser target = UserArgument.getUser(c, "user");

                            if(!ownership.currentlyOwnsShop()) throw FtcExceptionProvider.noShopOwned();
                            FtcExceptionProvider.checkNotBlocked(user, target);
                            FtcExceptionProvider.checkNotBlockedBy(user, target);

                            Markets region = Crown.getMarkets();
                            MarketShop shop = region.get(ownership.getOwnedName());

                            boolean trusted = shop.getCoOwners().contains(target.getUniqueId());

                            if(trusted) region.untrust(shop, target.getUniqueId());
                            else region.trust(shop, target.getUniqueId());

                            user.sendMessage(
                                    Component.translatable("market." + (trusted ? "un" : "") + "trust.sender",
                                            NamedTextColor.GOLD,
                                            target.nickDisplayName().color(NamedTextColor.YELLOW)
                                    )
                            );

                            target.sendOrMail(
                                    Component.translatable("market." + (trusted ? "un" : "") + "trust.target",
                                            NamedTextColor.GOLD,
                                            user.nickDisplayName().color(NamedTextColor.YELLOW)
                                    )
                            );

                            return 0;
                        })
                );
    }
}