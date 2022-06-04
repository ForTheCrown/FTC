package net.forthecrown.commands.markets;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.MarketArgument;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.user.CrownUser;
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
        super("MarketWarning");

        setPermission(Permissions.POLICE);
        setAliases("shopwarn", "marketwarn", "shopwarning");

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
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> CompletionProvider.suggestMatching(builder, GENERIC_REASONS))

                                        .executes(c -> {
                                            MarketShop shop = g.get(c, ARG_NAME);

                                            if (shop.markedForEviction()) {
                                                throw FtcExceptionProvider.create("That shop is already marked for eviction");
                                            }

                                            String reason = StringArgumentType.getString(c, "reason");
                                            Component cReason = FtcFormatter.formatString(reason);
                                            long delay = TimeArgument.getMillis(c, "time");
                                            long evictionTime = System.currentTimeMillis() + delay;

                                            Crown.getMarkets().beginEviction(shop, evictionTime, false, cReason);

                                            c.getSource().sendAdmin(
                                                    Component.text("Issued eviction notice to ")
                                                            .append(MarketDisplay.displayName(shop))
                                                            .append(Component.text(". Will be evicted in "))
                                                            .append(new TimePrinter(delay))
                                                            .append(Component.text(" or on "))
                                                            .append(FtcFormatter.formatDate(evictionTime))
                                                            .append(Component.text(". Reason: "))
                                                            .append(cReason)
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("undo")
                                .executes(c -> {
                                    MarketShop shop = g.get(c, ARG_NAME);

                                    if(!shop.markedForEviction()) {
                                        throw FtcExceptionProvider.create(shop.getName() + " is not marked for eviction");
                                    }

                                    Crown.getMarkets().stopEviction(shop);

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
                return UserArgument.user();
            }

            @Override
            public MarketShop get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException {
                CrownUser user = UserArgument.getUser(c, argName);
                MarketShop shop = Crown.getMarkets().get(user.getUniqueId());

                if(shop == null) {
                    throw FtcExceptionProvider.create(user.getName() + " doesn't own a shop");
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
                return MarketArgument.market();
            }

            @Override
            public MarketShop get(CommandContext<CommandSource> c, String argName) throws CommandSyntaxException {
                MarketShop shop = c.getArgument(argName, MarketShop.class);

                if(!shop.hasOwner()) {
                    throw FtcExceptionProvider.create(shop.getName() + " has no owner");
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