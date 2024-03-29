package net.forthecrown.economy.market.commands;

import static net.kyori.adventure.text.Component.text;

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
import net.forthecrown.McConstants;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3i;

public class CommandMarket extends FtcCommand {

  private final MarketManager manager;

  public CommandMarket(MarketManager manager) {
    super("market");

    this.manager = manager;

    setPermission(EconPermissions.MARKETS_ADMIN);
    setDescription("General purpose commands to manage markets");
    setAliases("markets", "shops");

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
  public void populateUsages(UsageFactory factory) {
    factory.usage("refresh_all")
        .addInfo("Refreshes all existing shops")
        .addInfo("This will just ensure every shop's entrance")
        .addInfo("exists and is displaying the correct info");

    factory.usage("create <region name>")
        .addInfo("Creates a new shop, linked to <region name>.")
        .addInfo("Every shop has to be linked to a world guard")
        .addInfo("region for membership and protection");

    factory.usage("list", "Lists all existing markets");

    var prefixed = factory.withPrefix("<shop name>");

    prefixed.usage("")
        .addInfo("Lists info about the <shop>");

    prefixed.usage("claim <user>")
        .addInfo("Claims the <shop> for the <user>.")
        .addInfo("Only works if the shop is not already claimed");

    prefixed.usage("connections <other shop> <add | remove>")
        .addInfo("Adds/removes a 'connection' with another")
        .addInfo("shop. Connections allow shops to be merged");

    prefixed.usage("delete")
        .addInfo("Deletes the shop");

    var ent = prefixed.withPrefix("entrances");
    ent.usage("add")
        .addInfo("Adds a shop entrance, with the notice set")
        .addInfo("as your position, the sign position as the")
        .addInfo("block above you and the direction being the")
        .addInfo("direction you're facing");

    ent.usage("add <notice: x,y,z> <sign: x,y,z> [<direction>]")
        .addInfo("Adds a shop entrance with the given parameters")
        .addInfo("If [direction] is not set, uses your direction");

    ent.usage("remove <index>")
        .addInfo("Removes the entrance with the given index");

    ent.usage("merge <other shop>")
        .addInfo("Merges <shop name> with <other shop>");

    prefixed.usage("unmerge", "Unmerges the shop");

    prefixed.usage("price <price: number(-1..)>")
        .addInfo("Sets the shop's price, 0 means free")
        .addInfo("If the price is -1, then the shop will use")
        .addInfo("a default price set in the markets config");

    prefixed.usage("wg_region")
        .addInfo("Displays info about the <shop>'s world guard region");

    prefixed.usage("unclaim", "Unclaims the shop, just removes the owner");

    prefixed.usage("unclaim_complete")
        .addInfo("Unclaims the shop, removes the owner and then")
        .addInfo("resets the shop");

    prefixed.usage("reset")
        .addInfo("Resets the shop");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("refresh_all")
            .executes(c -> {
              for (MarketShop s : manager.getAllShops()) {
                s.refresh(Markets.getWorld());
              }

              c.getSource().sendSuccess(text("Refreshed all shops"));
              return 0;
            })
        )

        .then(literal("create")
            .then(argument("wg_region", StringArgumentType.word())
                .suggests((context, builder) -> {
                  World world = Markets.getWorld();

                  RegionManager manager = WorldGuard.getInstance()
                      .getPlatform()
                      .getRegionContainer()
                      .get(BukkitAdapter.adapt(Markets.getWorld()));

                  return Completions.suggest(builder, manager.getRegions().keySet());
                })

                .executes(c -> {
                  //Get region
                  String rgName = c.getArgument("wg_region", String.class);
                  RegionManager manager = WorldGuard.getInstance()
                      .getPlatform()
                      .getRegionContainer()
                      .get(BukkitAdapter.adapt(Markets.getWorld()));

                  ProtectedRegion region = manager.getRegion(rgName);

                  if (region == null) {
                    throw Exceptions.format("Unknown WorldGuard region: '{0}'", rgName);
                  }

                  MarketShop shop = new MarketShop(region);
                  this.manager.add(shop);

                  c.getSource().sendSuccess(text("Created market shop tied to region '" + rgName + '\''));
                  return 0;
                })
            )
        )

        //List all the region names
        .then(literal("list")
            .executes(c -> {
              if (manager.isEmpty()) {
                throw EconExceptions.NO_SHOPS_EXIST;
              }

              TextComponent.Builder builder = text()
                  .content("Market shops:");

              for (MarketShop s : manager.getAllShops()) {
                builder
                    .append(Component.newline())
                    .append(MarketDisplay.displayName(s));
              }

              c.getSource().sendMessage(builder.build());
              return 0;
            })
        )

        .then(argument("market_shop", MarketCommands.argument)
            .executes(c -> {
              c.getSource().sendMessage(MarketDisplay.infoText(get(c)));
              return 0;
            })

            .then(literal("wg_region")
                .executes(c -> {
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
                  shop.refresh(Markets.getWorld());

                  c.getSource().sendSuccess(
                      text("Refreshed shop ")
                          .append(MarketDisplay.displayName(shop))
                  );
                  return 0;
                })
            )

            .then(literal("reset")
                .executes(c -> {
                  MarketShop shop = get(c);
                  shop.reset();

                  c.getSource().sendSuccess(
                      text("Reset ")
                          .append(MarketDisplay.displayName(shop))
                  );
                  return 0;
                })
            )

            .then(literal("unclaim")
                .executes(c -> {
                  MarketShop shop = get(c);
                  shop.unclaim(false);

                  c.getSource().sendSuccess(
                      text("Unclaimed ").append(MarketDisplay.displayName(shop))
                  );
                  return 0;
                })
            )

            .then(literal("unclaim_complete")
                .executes(c -> {
                  MarketShop shop = get(c);
                  shop.unclaim(true);

                  c.getSource().sendSuccess(
                      text("Evicted owner of ").append(MarketDisplay.displayName(shop))
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

                      c.getSource().sendSuccess(
                          text("Claimed shop ")
                              .append(MarketDisplay.displayName(shop))
                              .append(text(" for "))
                              .append(user.displayName())
                      );
                      return 0;
                    })
                )
            )

            .then(literal("delete")
                .executes(c -> {
                  MarketShop shop = get(c);
                  manager.remove(shop);

                  c.getSource().sendSuccess(text("Deleted shop " + shop.getName()));
                  return 0;
                })
            )

            .then(literal("connections")
                .then(argument("other_shop", MarketCommands.argument)
                    .then(literal("add")
                        .executes(c -> {
                          MarketShop shop = get(c);
                          MarketShop other = c.getArgument("other_shop", MarketShop.class);

                          shop.connect(other);

                          c.getSource().sendSuccess(
                              text("Connected ")
                                  .append(MarketDisplay.displayName(shop))
                                  .append(text(" and "))
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

                          c.getSource().sendSuccess(
                              text("Disconnected ")
                                  .append(MarketDisplay.displayName(shop))
                                  .append(text(" and "))
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
                          text("Set price of ")
                              .append(MarketDisplay.displayName(shop))
                              .append(text(" to "))
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

                  c.getSource().sendSuccess(
                      text("Unmerged ").append(MarketDisplay.displayName(shop))
                  );
                  return 0;
                })
            )

            .then(literal("merge")
                .then(argument("shop", MarketCommands.argument)
                    .executes(c -> {
                      MarketShop shop = get(c);
                      MarketShop other = c.getArgument("shop", MarketShop.class);

                      shop.merge(other);

                      c.getSource().sendSuccess(
                          text("Merged ")
                              .append(MarketDisplay.displayName(shop))
                              .append(text(" with "))
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

                          if (index <= 0 || index > shop.getEntrances().size()) {
                            throw Exceptions.invalidIndex(index + 1, shop.getEntrances().size());
                          }

                          shop.removeEntrance(index);

                          c.getSource().sendSuccess(
                              text("Removed entrance with index " + index + " from ")
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

                    .then(argument("notice_pos", ArgumentTypes.blockPosition())
                        .then(argument("doorSign_pos", ArgumentTypes.blockPosition())
                            .executes(c -> {
                              Player player = c.getSource().asPlayer();
                              BlockFace face = player.getFacing();

                              return addEntrance(c, face, false);
                            })

                            .then(argument("dir", ArgumentTypes.enumType(BlockFace.class))
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

  int addEntrance(CommandContext<CommandSource> c, BlockFace face, boolean usePlayerPos)
      throws CommandSyntaxException {
    MarketShop shop = get(c);

    Location noticeLoc = usePlayerPos ?
        c.getSource().asPlayer().getLocation().add(0, 1, 0)
        : ArgumentTypes.getLocation(c, "notice_pos");

    Vector3i doorSignPos = usePlayerPos ?
        figureSignLoc(c.getSource().asPlayer())
        : Vectors.intFrom(ArgumentTypes.getLocation(c, "doorSign_pos"));

    Vector3i noticePos = Vectors.intFrom(noticeLoc);
    ShopEntrance e = new ShopEntrance(face, noticePos, doorSignPos);

    shop.addEntrance(e);

    c.getSource().sendSuccess(
        text("Added ")
            .append(MarketDisplay.entranceDisplay(e))
            .append(text(" to "))
            .append(MarketDisplay.displayName(shop))
    );
    return 0;
  }

  Vector3i figureSignLoc(Player player) {
    World w = player.getWorld();
    Vector3i pos = Vectors.intFrom(player.getLocation());

    BlockFace face = player.getFacing();

    while (Vectors.getBlock(pos, w).isEmpty() && pos.y() < McConstants.MAX_Y) {
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