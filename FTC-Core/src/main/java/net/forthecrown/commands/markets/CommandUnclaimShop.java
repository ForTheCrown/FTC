package net.forthecrown.commands.markets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

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
                    CrownUser user = getUserSender(c);
                    check(user);

                    user.sendMessage(
                            Component.text()
                                    .append(Component.translatable("market.unclaim", NamedTextColor.GRAY))
                                    .append(Component.space())
                                    .append(
                                            Component.translatable("buttons.confirm", NamedTextColor.AQUA)
                                                    .hoverEvent(Component.translatable("market.unclaim.hover"))
                                                    .clickEvent(ClickEvent.runCommand('/' + getName() + " confirm"))
                                    )
                                    .append(Component.newline())
                                    .append(Component.translatable("market.unclaim2", NamedTextColor.RED))
                    );

                    return 0;
                })

                .then(literal("confirm")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            check(user);

                            Markets markets = Crown.getMarkets();
                            MarketShop shop = markets.get(user.getUniqueId());

                            markets.unclaim(shop, true);

                            user.sendMessage(
                                    Component.translatable("market.unclaimed", NamedTextColor.GRAY)
                            );
                            return 0;
                        })
                );
    }

    private void check(CrownUser user) throws CommandSyntaxException {
        UserMarketData ownership = user.getMarketData();

        if(!ownership.currentlyOwnsShop()) throw FtcExceptionProvider.noShopOwned();
        Markets.checkStatusChange(ownership);
    }
}