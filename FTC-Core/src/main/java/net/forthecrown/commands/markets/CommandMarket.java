package net.forthecrown.commands.markets;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.region.RegionPrintoutBuilder;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.forthecrown.commands.arguments.MarketArgument;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.market.*;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class CommandMarket extends FtcCommand {

    public CommandMarket() {
        super("market");

        setPermission(Permissions.ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /market
     *
     * Permissions used:
     * ftc.admin
     *
     * Main Author: Julie <3
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("save")
                        .executes(c -> {
                            Crown.getMarkets().save();

                            c.getSource().sendMessage("Saved markets");
                            return 0;
                        })
                )

                .then(literal("reload")
                        .executes(c -> {
                            Crown.getMarkets().reload();

                            c.getSource().sendMessage("Reloaded markets");
                            return 0;
                        })
                )

                .then(literal("refresh_all")
                        .executes(c -> {
                            Markets markets = Crown.getMarkets();

                            for (MarketShop s: markets.getAllShops()) {
                                markets.refresh(s);
                            }

                            c.getSource().sendAdmin("Refreshed all shops");
                            return 0;
                        })
                )

                .then(literal("create")
                        .then(argument("wg_region", StringArgumentType.word())
                                .executes(c -> {
                                    //Get region
                                    String rgName = c.getArgument("wg_region", String.class);
                                    RegionManager manager = WorldGuard.getInstance()
                                            .getPlatform()
                                            .getRegionContainer()
                                            .get(BukkitAdapter.adapt(Crown.getMarkets().getWorld()));

                                    ProtectedRegion region = manager.getRegion(rgName);

                                    if(region == null) throw FtcExceptionProvider.create("Unknown region: " + rgName);

                                    MarketShop shop = new FtcMarketShop(region);
                                    Crown.getMarkets().add(shop);

                                    c.getSource().sendAdmin("Created market shop tied to region '" + rgName + '\'');
                                    return 0;
                                })
                        )
                )

                //List all the region names
                .then(literal("list")
                        .executes(c -> {
                            Markets region = Crown.getMarkets();
                            if(region.isEmpty()) throw FtcExceptionProvider.create("No shops exist");

                            TextComponent.Builder builder = Component.text()
                                    .content("Market shops:");

                            for (MarketShop s: region.getAllShops()) {
                                builder
                                        .append(Component.newline())
                                        .append(MarketDisplay.displayName(s));
                            }

                            c.getSource().sendMessage(builder.build());
                            return 0;
                        })
                )

                .then(argument("market_shop", MarketArgument.market())
                        .executes(c -> {
                            c.getSource().sendMessage(MarketDisplay.infoText(get(c)));
                            return 0;
                        })

                        .then(literal("wg_region")
                                .executes(c -> {
                                    Markets region = Crown.getMarkets();
                                    MarketShop shop = get(c);

                                    Actor actor = BukkitAdapter.adapt(c.getSource().asBukkit());

                                    RegionPrintoutBuilder builder = new RegionPrintoutBuilder(
                                            region.getWorld().getName(),
                                            shop.getWorldGuard(),
                                            WorldGuard.getInstance().getProfileCache(),
                                            actor
                                    );

                                    actor.print(builder.call());
                                    return 0;
                                })
                        )

                        .then(literal("refresh")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    Markets region = Crown.getMarkets();

                                    region.refresh(shop);

                                    c.getSource().sendAdmin(
                                            Component.text("Refreshed shop ")
                                                    .append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("unclaim")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    Markets region = Crown.getMarkets();

                                    region.unclaim(shop, false);

                                    c.getSource().sendAdmin(
                                            Component.text("Unclaimed ").append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("unclaim_complete")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    Markets region = Crown.getMarkets();

                                    region.unclaim(shop, true);

                                    c.getSource().sendAdmin(
                                            Component.text("Evicted owner of ").append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("claim")
                                .then(argument("owner", UserArgument.user())
                                        .executes(c -> {
                                            MarketShop shop = get(c);
                                            Markets region = Crown.getMarkets();
                                            CrownUser user = UserArgument.getUser(c, "owner");

                                            region.claim(shop, user);

                                            c.getSource().sendAdmin(
                                                    Component.text("Claimed shop ")
                                                            .append(MarketDisplay.displayName(shop))
                                                            .append(Component.text(" for "))
                                                            .append(user.nickDisplayName())
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("delete")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    Markets region = Crown.getMarkets();

                                    region.remove(shop);

                                    c.getSource().sendAdmin("Deleted shop " + shop.getName());
                                    return 0;
                                })
                        )

                        .then(literal("connections")
                                .then(argument("other_shop", MarketArgument.market())
                                        .then(literal("add")
                                                .executes(c -> {
                                                    MarketShop shop = get(c);
                                                    MarketShop other = c.getArgument("other_shop", MarketShop.class);
                                                    Markets region = Crown.getMarkets();

                                                    region.connect(shop, other);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Connected ")
                                                                    .append(MarketDisplay.displayName(shop))
                                                                    .append(Component.text(" and "))
                                                                    .append(MarketDisplay.displayName(other))
                                                    );
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .executes(c -> {
                                                    MarketShop shop = get(c);
                                                    MarketShop other = c.getArgument("other_shop", MarketShop.class);
                                                    Markets region = Crown.getMarkets();

                                                    region.disconnect(shop, other);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Disconnected ")
                                                                    .append(MarketDisplay.displayName(shop))
                                                                    .append(Component.text(" and "))
                                                                    .append(MarketDisplay.displayName(other))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("price")
                                .then(argument("price_actual", IntegerArgumentType.integer(-1, ComVars.getMaxMoneyAmount()))
                                        .executes(c -> {
                                            MarketShop shop = get(c);
                                            int price = c.getArgument("price_actual", Integer.class);

                                            shop.setPrice(price);

                                            c.getSource().sendMessage(
                                                    Component.text("Set price of ")
                                                            .append(MarketDisplay.displayName(shop))
                                                            .append(Component.text(" to "))
                                                            .append(FtcFormatter.rhines(price))
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("unmerge")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    Markets region = Crown.getMarkets();

                                    region.unmerge(shop);

                                    c.getSource().sendAdmin(
                                            Component.text("Unmerged ").append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("merge")
                                .then(argument("shop", MarketArgument.market())
                                        .executes(c -> {
                                            MarketShop shop = get(c);
                                            MarketShop other = c.getArgument("shop", MarketShop.class);
                                            Markets region = Crown.getMarkets();

                                            region.merge(shop, other);

                                            c.getSource().sendAdmin(
                                                    Component.text("Merged ")
                                                            .append(MarketDisplay.displayName(shop))
                                                            .append(Component.text(" with "))
                                                            .append(MarketDisplay.displayName(other))
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("entrances")
                                .then(literal("remove")
                                        .then(argument("index", IntegerArgumentType.integer(0))
                                                .executes(c -> {
                                                    MarketShop shop = get(c);
                                                    int index = c.getArgument("index", Integer.class);

                                                    Markets region = Crown.getMarkets();

                                                    region.removeEntrance(shop, index);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Removed entrance with index " + index + " from ")
                                                                    .append(MarketDisplay.displayName(shop))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("add")
                                        .executes(c -> {
                                            Player player = c.getSource().asPlayer();
                                            BlockFace face = player.getFacing();

                                            return addEntrance(c, face, true);
                                        })

                                        .then(argument("notice_pos", PositionArgument.blockPos())
                                                .then(argument("doorSign_pos", PositionArgument.blockPos())
                                                        .executes(c -> {
                                                            Player player = c.getSource().asPlayer();
                                                            BlockFace face = player.getFacing();

                                                            return addEntrance(c, face, false);
                                                        })

                                                        .then(argument("dir", EnumArgument.of(BlockFace.class))
                                                                .executes(c -> {
                                                                    BlockFace face = c.getArgument("dir", BlockFace.class);

                                                                    return addEntrance(c, face, false);
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    int addEntrance(CommandContext<CommandSource> c, BlockFace face, boolean usePlayerPos) throws CommandSyntaxException {
        MarketShop shop = get(c);
        Markets region = Crown.getMarkets();

        Location noticeLoc = usePlayerPos ? c.getSource().asPlayer().getLocation().add(0, 1, 0) : PositionArgument.getLocation(c, "notice_pos");
        Location doorSignLoc = usePlayerPos ? figureSignLoc(c.getSource().asPlayer()) : PositionArgument.getLocation(c, "doorSign_pos");

        Vector3i noticePos = Vector3i.of(noticeLoc);
        Vector3i doorSignPos = Vector3i.of(doorSignLoc);
        ShopEntrance e = new ShopEntrance(face, noticePos, doorSignPos);

        region.addEntrance(shop, e);

        c.getSource().sendAdmin(
                Component.text("Added ")
                        .append(MarketDisplay.displayName(e))
                        .append(Component.text(" to "))
                        .append(MarketDisplay.displayName(shop))
        );
        return 0;
    }

    Location figureSignLoc(Player player) {
        WorldVec3i pos = WorldVec3i.of(player);
        BlockFace face = player.getFacing();

        while (pos.getMaterial().isAir() && pos.getY() < FtcUtils.MAX_Y) {
            pos = pos.above();
        }

        int iteration = 0;
        while (pos.getMaterial().isSolid() && iteration < 10) {
            pos = pos.inDirection(face);
            iteration++;
        }

        return pos.toLocation();
    }

    MarketShop get(CommandContext<CommandSource> c) {
        return c.getArgument("market_shop", MarketShop.class);
    }
}