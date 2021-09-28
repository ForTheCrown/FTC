package net.forthecrown.commands.markets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

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
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");

                            check(user, target);

                            user.sendMessage(
                                    Component.text()
                                            .append(Component.translatable("market.transfer",
                                                    NamedTextColor.GRAY,
                                                    target.nickDisplayName()
                                                            .color(NamedTextColor.YELLOW)
                                            ))
                                            .append(Component.space())
                                            .append(
                                                    Component.translatable("buttons.confirm", NamedTextColor.AQUA)
                                                            .hoverEvent(Component.translatable("market.transfer.hover"))
                                                            .clickEvent(ClickEvent.runCommand('/' + getName() + ' ' + target.getName() + " confirm"))
                                            )
                                            .append(Component.newline())
                                            .append(Component.translatable("market.transfer2", NamedTextColor.RED))
                                            .build()
                            );

                            return 0;
                        })

                        .then(literal("confirm")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    CrownUser target = UserArgument.getUser(c, "user");

                                    check(user, target);

                                    Markets markets = Crown.getMarkets();
                                    MarketShop shop = markets.get(user.getUniqueId());

                                    markets.transfer(shop, target.getUniqueId());

                                    user.sendMessage(
                                            Component.translatable("market.transferred",
                                                    NamedTextColor.YELLOW,
                                                    user.nickDisplayName()
                                                            .color(NamedTextColor.GOLD)
                                            )
                                    );

                                    target.sendOrMail(
                                            Component.translatable("market.transferred.target",
                                                    NamedTextColor.YELLOW,
                                                    user.nickDisplayName()
                                                            .color(NamedTextColor.GOLD)
                                            )
                                    );

                                    return 0;
                                })
                        )
                );
    }

    private void check(CrownUser user, CrownUser target) throws CommandSyntaxException {
        if(!user.getMarketOwnership().currentlyOwnsShop()) throw FtcExceptionProvider.noShopOwned();

        if(target.getMarketOwnership().currentlyOwnsShop()) {
            throw FtcExceptionProvider.translatable("market.transfer.error.targetHasShop");
        }

        Markets.checkCanChangeStatus(user.getMarketOwnership());

        if(!target.getMarketOwnership().canChangeStatus()) {
            throw FtcExceptionProvider.translatable("market.transfer.error.targetStatus", target.nickDisplayName());
        }
    }
}