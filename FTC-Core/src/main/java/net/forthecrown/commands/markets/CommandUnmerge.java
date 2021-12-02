package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.MarketOwnership;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
                    CrownUser user = getUserSender(c);
                    MarketOwnership ownership = user.getMarketOwnership();

                    if(!ownership.currentlyOwnsShop()) throw FtcExceptionProvider.noShopOwned();

                    Markets markets = Crown.getMarkets();
                    MarketShop shop = markets.get(user.getUniqueId());

                    if(!shop.isMerged()) throw FtcExceptionProvider.translatable("market.merge.notMerged");

                    CrownUser target = shop.getMerged().ownerUser();
                    target.unloadIfOffline();

                    markets.unmerge(shop);

                    target.sendOrMail(
                            Component.translatable("market.unmerge.target",
                                    NamedTextColor.GRAY,
                                    user.nickDisplayName()
                                            .color(NamedTextColor.YELLOW)
                            )
                    );

                    user.sendMessage(
                            Component.translatable("market.unmerge.sender",
                                    NamedTextColor.GRAY,
                                    target.nickDisplayName()
                                            .color(NamedTextColor.YELLOW)
                            )
                    );

                    return 0;
                });
    }
}