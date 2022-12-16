package net.forthecrown.commands.markets;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.core.Messages;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;

public class CommandMarketWarning extends FtcCommand {
    public static final List<String> GENERIC_REASONS = Arrays.asList(
            "Shop doesn't sell enough",
            "Shop is too empty",
            "Shop is not decorated",
            "Shop is being used as a farm"
    );

    public CommandMarketWarning() {
        super("marketevict");

        setPermission(Permissions.MARKET_WARNING);
        setAliases("shopevict", "evictshop", "evictmarket");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows a staff member to mark a shop for
     * eviction or cancel an eviction notice. Shop
     * will be automatically unclaimed and reset
     * in the interval given by the evictionCleanupTime
     * ComVar
     *
     * Valid usages of command:
     * /MarketWarning <user | market> <name> undo
     * /MarketWarning <user | market> <name> <reason>
     *
     * Permissions used: ftc.police
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(getterArg(MarketGetter.MARKET_NAME))
                .then(getterArg(MarketGetter.USER));
    }

    private static final String ARG_NAME = "market";

    private LiteralArgumentBuilder<CommandSource> getterArg(MarketGetter g) {
        return literal(g.getName())
                .then(argument(ARG_NAME, g.getArgument())
                        .then(argument("time", TimeArgument.time())
                                .then(argument("reason", Arguments.CHAT)
                                        .suggests(suggestMatching(GENERIC_REASONS))

                                        .executes(c -> {
                                            MarketShop shop = g.get(c, ARG_NAME);

                                            if (shop.markedForEviction()) {
                                                throw Exceptions.MARKED_EVICTION;
                                            }

                                            Component reason = c.getArgument("reason", Component.class);
                                            long delay = TimeArgument.getMillis(c, "time");
                                            long evictionTime = System.currentTimeMillis() + delay;

                                            var owner = shop.ownerUser();

                                            shop.beginEviction(evictionTime, reason, c.getSource().textName());

                                            c.getSource().sendAdmin(
                                                    Messages.issuedEviction(owner, evictionTime, reason)
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("undo")
                                .executes(c -> {
                                    MarketShop shop = g.get(c, ARG_NAME);

                                    if(!shop.markedForEviction()) {
                                        throw Exceptions.NOT_MARKED_EVICTION;
                                    }

                                    shop.stopEviction();

                                    c.getSource().sendAdmin(
                                            Component.text()
                                                    .append(MarketDisplay.displayName(shop))
                                                    .append(Component.text(" is no longer marked for eviction"))
                                                    .build()
                                    );
                                    return 0;
                                })
                        )
                );
    }

    public interface MarketGetter {
        MarketGetter USER = new MarketGetter() {
            @Override
            public ArgumentType<?> getArgument() {
                return Arguments.USER;
            }

            @Override
            public MarketShop get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException {
                User user = Arguments.getUser(c, argName);
                MarketShop shop = Economy.get().getMarkets().get(user.getUniqueId());

                if (shop == null) {
                    throw Exceptions.noShopOwned(user);
                }

                return shop;
            }

            @Override
            public String getName() {
                return "user";
            }
        };

        MarketGetter MARKET_NAME = new MarketGetter() {
            @Override
            public ArgumentType<?> getArgument() {
                return Arguments.MARKET;
            }

            @Override
            public MarketShop get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException {
                MarketShop shop = c.getArgument(argName, MarketShop.class);

                if (!shop.hasOwner()) {
                    throw Exceptions.shopNotOwned(shop);
                }

                return shop;
            }

            @Override
            public String getName() {
                return "market";
            }
        };

        ArgumentType<?> getArgument();
        MarketShop get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException;
        String getName();
    }
}