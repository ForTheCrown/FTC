package net.forthecrown.commands.markets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.data.UserMarketData;

import static net.forthecrown.core.Messages.MARKET_MERGE_BLOCKED_SENDER;
import static net.forthecrown.core.Messages.MARKET_MERGE_BLOCKED_TARGET;

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
                .then(argument("user", Arguments.ONLINE_USER)

                        // mergeshop <user>
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");

                            MarketMerger r = new MarketMerger(user, target);

                            if (r.testCanSend()) {
                                r.send();
                            }

                            return 0;
                        })

                        // mergeshop <user> confirm
                        .then(literal("confirm")
                                .executes(c -> {
                                    User user = getUserSender(c);
                                    User target = Arguments.getUser(c, "user");

                                    MarketMerger r = new MarketMerger(user, target);

                                    if (r.test()) {
                                        r.confirm();
                                    }

                                    return 0;
                                })
                        )

                        // mergeshop <user> deny
                        .then(literal("deny")
                                .executes(c -> {
                                    User user = getUserSender(c);
                                    User target = Arguments.getUser(c, "user");

                                    if (!user.getMarketData().hasIncoming(target.getUniqueId())) {
                                        throw Exceptions.noIncoming(target);
                                    }

                                    target.getMarketData().setOutgoing(null);
                                    user.getMarketData().removeIncoming(target.getUniqueId());

                                    user.sendMessage(Messages.REQUEST_DENIED);
                                    target.sendMessage(Messages.requestDenied(user));
                                    return 0;
                                })
                        )

                        // mergeshop <user> cancel
                        .then(literal("cancel")
                                .executes(c -> {
                                    User user = getUserSender(c);
                                    User target = Arguments.getUser(c, "user");

                                    if (!target.getUniqueId().equals(user.getMarketData().getOutgoing())) {
                                        throw Exceptions.noOutgoing(user);
                                    }

                                    user.getMarketData().setOutgoing(null);
                                    target.getMarketData().removeIncoming(target.getUniqueId());

                                    user.sendMessage(Messages.REQUEST_CANCELLED);
                                    target.sendMessage(Messages.requestCancelled(user));
                                    return 0;
                                })
                        )
                );
    }

    private static class MarketMerger {
        final User user;
        final UserInteractions inter;
        final UserMarketData ownership;

        final User target;
        final UserInteractions targetInter;
        final UserMarketData targetOwnership;

        final MarketManager markets;
        final MarketShop shop;
        final MarketShop targetShop;

        private MarketMerger(User user, User target) {
            this.user = user;
            this.inter = user.getInteractions();
            this.ownership = user.getMarketData();

            this.target = target;
            this.targetInter = target.getInteractions();
            this.targetOwnership = target.getMarketData();

            this.markets = Economy.get().getMarkets();
            this.shop = markets.get(user.getUniqueId());
            this.targetShop = markets.get(target.getUniqueId());
        }

        void send() {
            targetOwnership.addIncoming(user.getUniqueId());
            ownership.setOutgoing(target.getUniqueId());

            target.sendMessage(Messages.marketMergeTarget(user));

            user.sendMessage(
                    Messages.requestSent(
                            target,
                            Messages.crossButton("/mergeshop %s cancel", target.getName())
                    )
            );
        }

        void confirm() {
            // In the confirm case, the target and user are
            // switched in the definition that the user is
            // not the sender of the merge request, but rather
            // the target is, as the user performed the confirm
            // command
            ownership.removeIncoming(target.getUniqueId());
            targetOwnership.setOutgoing(null);

            shop.merge(targetShop);

            user.sendMessage(Messages.marketMerged(target));
            target.sendMessage(Messages.marketMerged(user));

            target.unloadIfOffline();
        }

        boolean testCanSend() throws CommandSyntaxException {
            if (!test()) {
                return false;
            }

            if (ownership.getOutgoing() != null) {
                User user = Users.get(targetOwnership.getOutgoing());
                user.unloadIfOffline();

                throw Exceptions.requestAlreadySent(target);
            }

            return true;
        }

        boolean test() throws CommandSyntaxException {
            if (target.equals(user)) {
                throw Exceptions.MERGE_SELF;
            }

            Users.testBlockedException(user, target,
                    MARKET_MERGE_BLOCKED_SENDER,
                    MARKET_MERGE_BLOCKED_TARGET
            );

            if (!Markets.ownsShop(user)) {
                throw Exceptions.NO_SHOP_OWNED;
            }

            if (shop.isMerged()) {
                throw Exceptions.ALREADY_MERGED;
            }

            if (targetShop.isMerged()) {
                throw Exceptions.marketTargetMerged(target);
            }

            if (!Markets.ownsShop(target)) {
                throw Exceptions.marketTargetHasShop(target);
            }

            if (!shop.isConnected(targetShop)) {
                throw Exceptions.marketNotConnected(target);
            }

            return true;
        }
    }
}