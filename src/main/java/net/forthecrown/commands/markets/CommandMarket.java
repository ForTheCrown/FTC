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
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.*;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3i;

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
                .then(literal("refresh_all")
                        .executes(c -> {
                            MarketManager markets = Economy.get().getMarkets();

                            for (MarketShop s: markets.getAllShops()) {
                                s.refresh(Markets.getWorld());
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
                                            .get(BukkitAdapter.adapt(Markets.getWorld()));

                                    ProtectedRegion region = manager.getRegion(rgName);

                                    if(region == null) {
                                        throw Exceptions.unknownRegion(rgName);
                                    }

                                    MarketShop shop = new MarketShop(region);
                                    Economy.get().getMarkets().add(shop);

                                    c.getSource().sendAdmin("Created market shop tied to region '" + rgName + '\'');
                                    return 0;
                                })
                        )
                )

                //List all the region names
                .then(literal("list")
                        .executes(c -> {
                            MarketManager region = Economy.get().getMarkets();

                            if(region.isEmpty()) {
                                throw Exceptions.NO_SHOPS_EXIST;
                            }

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

                .then(argument("market_shop", Arguments.MARKET)
                        .executes(c -> {
                            c.getSource().sendMessage(MarketDisplay.infoText(get(c)));
                            return 0;
                        })

                        .then(literal("wg_region")
                                .executes(c -> {
                                    MarketManager region = Economy.get().getMarkets();
                                    MarketShop shop = get(c);

                                    Actor actor = BukkitAdapter.adapt(c.getSource().asBukkit());

                                    RegionPrintoutBuilder builder = new RegionPrintoutBuilder(
                                            Markets.getWorld().getName(),
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
                                    MarketManager region = Economy.get().getMarkets();

                                    shop.refresh(Markets.getWorld());

                                    c.getSource().sendAdmin(
                                            Component.text("Refreshed shop ")
                                                    .append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("reset")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    MarketManager region = Economy.get().getMarkets();

                                    shop.reset();

                                    c.getSource().sendAdmin(
                                            Component.text("Reset ")
                                                    .append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("unclaim")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    shop.unclaim(false);

                                    c.getSource().sendAdmin(
                                            Component.text("Unclaimed ").append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("unclaim_complete")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    shop.unclaim(true);

                                    c.getSource().sendAdmin(
                                            Component.text("Evicted owner of ").append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("claim")
                                .then(argument("owner", Arguments.USER)
                                        .executes(c -> {
                                            MarketShop shop = get(c);
                                            User user = Arguments.getUser(c, "owner");
                                            shop.claim(user);

                                            c.getSource().sendAdmin(
                                                    Component.text("Claimed shop ")
                                                            .append(MarketDisplay.displayName(shop))
                                                            .append(Component.text(" for "))
                                                            .append(user.displayName())
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("delete")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    MarketManager region = Economy.get().getMarkets();

                                    region.remove(shop);

                                    c.getSource().sendAdmin("Deleted shop " + shop.getName());
                                    return 0;
                                })
                        )

                        .then(literal("connections")
                                .then(argument("other_shop", Arguments.MARKET)
                                        .then(literal("add")
                                                .executes(c -> {
                                                    MarketShop shop = get(c);
                                                    MarketShop other = c.getArgument("other_shop", MarketShop.class);

                                                    shop.connect(other);

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

                                                    shop.disconnect(other);

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
                                .then(argument("price_actual", IntegerArgumentType.integer(-1))
                                        .executes(c -> {
                                            MarketShop shop = get(c);
                                            int price = c.getArgument("price_actual", Integer.class);

                                            shop.setPrice(price);

                                            c.getSource().sendMessage(
                                                    Component.text("Set price of ")
                                                            .append(MarketDisplay.displayName(shop))
                                                            .append(Component.text(" to "))
                                                            .append(UnitFormat.rhines(price))
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("unmerge")
                                .executes(c -> {
                                    MarketShop shop = get(c);
                                    shop.unmerge();

                                    c.getSource().sendAdmin(
                                            Component.text("Unmerged ").append(MarketDisplay.displayName(shop))
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("merge")
                                .then(argument("shop", Arguments.MARKET)
                                        .executes(c -> {
                                            MarketShop shop = get(c);
                                            MarketShop other = c.getArgument("shop", MarketShop.class);

                                            shop.merge(other);

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
                                        .then(argument("index", IntegerArgumentType.integer(1))
                                                .executes(c -> {
                                                    MarketShop shop = get(c);
                                                    int index = c.getArgument("index", Integer.class) - 1;

                                                    if (index <= 0|| index > shop.getEntrances().size()) {
                                                        throw Exceptions.invalidIndex(index + 1, shop.getEntrances().size());
                                                    }

                                                    shop.removeEntrance(index);

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

        Location noticeLoc = usePlayerPos ?
                c.getSource().asPlayer().getLocation().add(0, 1, 0)
                : PositionArgument.getLocation(c, "notice_pos");

        Vector3i doorSignPos = usePlayerPos ?
                figureSignLoc(c.getSource().asPlayer())
                : Vectors.intFrom(PositionArgument.getLocation(c, "doorSign_pos"));

        Vector3i noticePos = Vectors.intFrom(noticeLoc);
        ShopEntrance e = new ShopEntrance(face, noticePos, doorSignPos);

        shop.addEntrance(e);

        c.getSource().sendAdmin(
                Component.text("Added ")
                        .append(MarketDisplay.entranceDisplay(e))
                        .append(Component.text(" to "))
                        .append(MarketDisplay.displayName(shop))
        );
        return 0;
    }

    Vector3i figureSignLoc(Player player) {
        World w = player.getWorld();
        Vector3i pos = Vectors.intFrom(player.getLocation());

        BlockFace face = player.getFacing();

        while (Vectors.getBlock(pos, w).isEmpty() && pos.y() < Util.MAX_Y) {
            pos = pos.add(0, 1, 0);
        }

        int iteration = 0;
        while (Vectors.getBlock(pos, w).isSolid() && iteration < 10) {
            pos = pos.add(face.getModX(), face.getModY(), face.getModZ());
            iteration++;
        }

        return pos;
    }

    MarketShop get(CommandContext<CommandSource> c) {
        return c.getArgument("market_shop", MarketShop.class);
    }
}