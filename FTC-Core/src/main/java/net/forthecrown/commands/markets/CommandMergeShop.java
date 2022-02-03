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
import net.forthecrown.user.UserMarketData;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class CommandMergeShop extends FtcCommand {

    public CommandMergeShop() {
        super("mergeshop");

        setPermission(Permissions.MARKETS);
        setAliases("mergemarket", "shopmerge", "marketmerge");
        setDescription("Request to merge your shop with someone else's");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /mergeshop <user>
     * /mergeshop <user> <cancel | confirm | deny>
     *
     * Permissions used:
     * ftc.markets
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");

                            MarketMerger r = new MarketMerger(user, target);
                            r.testCanSend();

                            send(r);
                            return 0;
                        })

                        .then(literal("confirm")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    CrownUser target = UserArgument.getUser(c, "user");

                                    MarketMerger r = new MarketMerger(user, target);
                                    r.test();

                                    confirm(r);
                                    return 0;
                                })
                        )

                        .then(literal("deny")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    CrownUser target = UserArgument.getUser(c, "user");

                                    if(!user.getMarketData().hasIncoming(target.getUniqueId())) {
                                        throw FtcExceptionProvider.translatable("market.merge.error.noIncoming", target.nickDisplayName());
                                    }

                                    target.getMarketData().setOutgoing(null);
                                    user.getMarketData().removeIncoming(target.getUniqueId());

                                    sendMessages(user, target, "market.merge.deny");
                                    return 0;
                                })
                        )

                        .then(literal("cancel")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    CrownUser target = UserArgument.getUser(c, "user");

                                    if(!target.getUniqueId().equals(user.getMarketData().getOutgoing())) {
                                        throw FtcExceptionProvider.translatable("market.merge.error.noOutgoing", target.nickDisplayName());
                                    }

                                    user.getMarketData().setOutgoing(null);
                                    target.getMarketData().removeIncoming(target.getUniqueId());

                                    sendMessages(user, target, "market.merge.cancel");
                                    return 0;
                                })
                        )
                );
    }

    private void sendMessages(CrownUser user, CrownUser target, String keyPrefix) {
        user.sendMessage(
                Component.translatable(keyPrefix + ".sender",
                        NamedTextColor.GRAY,
                        target.nickDisplayName()
                                .color(NamedTextColor.YELLOW)
                )
        );

        target.sendMessage(
                Component.translatable(keyPrefix + ".target",
                        NamedTextColor.GRAY,
                        user.nickDisplayName()
                                .color(NamedTextColor.YELLOW)
                )
        );
    }

    private void send(MarketMerger r) {
        r.targetOwnership.addIncoming(r.user.getUniqueId());
        r.ownership.setOutgoing(r.target.getUniqueId());

        r.target.sendMessage(
                Component.translatable("market.merge.request.target",
                        NamedTextColor.GRAY,
                        r.user.nickDisplayName().color(NamedTextColor.YELLOW)
                )
                        .append(Component.space())
                        .append(accept(r))
                        .append(Component.space())
                        .append(deny(r))
        );

        r.user.sendMessage(
                Component.translatable("market.merge.request.sender",
                        NamedTextColor.GRAY,
                        r.target.nickDisplayName().color(NamedTextColor.YELLOW)
                )
                        .append(Component.space())
                        .append(cancel(r))
        );

        r.target.unloadIfOffline();
    }

    private void confirm(MarketMerger r) {
        r.ownership.removeIncoming(r.target.getUniqueId());
        r.targetOwnership.setOutgoing(null);

        r.markets.merge(r.shop, r.targetShop);

        r.user.sendMessage(
                Component.translatable("market.merge.target",
                        NamedTextColor.YELLOW,
                        r.target.nickDisplayName()
                                .color(NamedTextColor.GOLD)
                )
        );

        r.target.sendOrMail(
                Component.translatable("market.merge.sender",
                        NamedTextColor.YELLOW,
                        r.user.nickDisplayName()
                                .color(NamedTextColor.GOLD)
                )
        );

        r.target.unloadIfOffline();
    }

    private Component cancel(MarketMerger r) {
        return Component.translatable("buttons.cancel", NamedTextColor.AQUA)
                .hoverEvent(Component.translatable("market.merge.cancel.hover"))
                .clickEvent(ClickEvent.runCommand('/' + getName() + ' ' + r.target.getName() + ' ' + "cancel"));
    }

    private Component accept(MarketMerger r) {
        return buttonComponent(r,
                "buttons.confirm",
                "market.merge.accept.hover",
                "confirm",
                NamedTextColor.GREEN
        );
    }

    private Component deny(MarketMerger r) {
        return buttonComponent(r,
                "buttons.deny",
                "market.merge.deny.hover",
                "deny",
                NamedTextColor.RED
        );
    }

    private Component buttonComponent(MarketMerger r, String key, String hoverKey, String cmdSuffix, TextColor color) {
        return Component.translatable(key, color)
                .hoverEvent(Component.translatable(hoverKey))
                .clickEvent(ClickEvent.runCommand('/' + getName() + ' ' + r.user.getName() + ' ' + cmdSuffix));
    }

    private static class MarketMerger implements Struct {
        final CrownUser user;
        final UserInteractions inter;
        final UserMarketData ownership;

        final CrownUser target;
        final UserInteractions targetInter;
        final UserMarketData targetOwnership;

        final Markets markets;
        final MarketShop shop;
        final MarketShop targetShop;

        private MarketMerger(CrownUser user, CrownUser target) {
            this.user = user;
            this.inter = user.getInteractions();
            this.ownership = user.getMarketData();

            this.target = target;
            this.targetInter = target.getInteractions();
            this.targetOwnership = target.getMarketData();

            this.markets = Crown.getMarkets();
            this.shop = markets.get(user.getUniqueId());
            this.targetShop = markets.get(target.getUniqueId());
        }

        private void testCanSend() throws CommandSyntaxException {
            test();

            if(ownership.getOutgoing() != null) {
                CrownUser user = UserManager.getUser(targetOwnership.getOutgoing());
                user.unloadIfOffline();

                throw FtcExceptionProvider.translatable("market.merge.error.alreadySent", user.nickDisplayName());
            }
        }

        private void test() throws CommandSyntaxException {
            if(target.equals(user)) throw FtcExceptionProvider.translatable("market.merge.error.self");

            FtcExceptionProvider.checkNotBlocked(user, target);
            FtcExceptionProvider.checkNotBlockedBy(user, target);

            if(!ownership.currentlyOwnsShop()) throw FtcExceptionProvider.noShopOwned();

            if(shop.isMerged()) throw FtcExceptionProvider.translatable("market.merge.alreadyMerged");
            if(targetShop.isMerged()) throw FtcExceptionProvider.translatable("market.merge.targetMerged", target.nickDisplayName());

            if(!targetOwnership.currentlyOwnsShop()) {
                throw FtcExceptionProvider.translatable("market.merge.error.targetNoShop", target.nickDisplayName());
            }

            if(!markets.areConnected(shop, targetShop)) {
                throw FtcExceptionProvider.translatable("market.merge.error.notConnected", target.nickDisplayName());
            }
        }
    }
}