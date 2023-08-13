package net.forthecrown.commands.economy;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.DataCommands;
import net.forthecrown.commands.DataCommands.DataAccessor;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandEditShop extends FtcCommand {

  private final Component usageMessage;

  public CommandEditShop() {
    super("editshop");

    usageMessage = makeUsageMessage();

    setDescription("Allows you to edit a shop");
    setAliases("shopedit", "signshop");
    setPermission(Permissions.SHOP_EDIT);

    register();
  }

  private final DataAccessor dataAccess = new DataAccessor() {
    @Override
    public CompoundTag getTag(CommandContext<CommandSource> context)
        throws CommandSyntaxException {
      var shop = getShop(context.getSource().asPlayer());
      CompoundTag tag = BinaryTags.compoundTag();
      shop.save(tag);
      return tag;
    }

    @Override
    public void setTag(CommandContext<CommandSource> context, CompoundTag tag)
        throws CommandSyntaxException {
      var shop = getShop(context.getSource().asPlayer());
      shop.load(tag);
    }
  };

  private Component makeUsageMessage() {
    final Component border = Component.text("                  ")
        .color(NamedTextColor.DARK_GRAY)
        .decorate(TextDecoration.STRIKETHROUGH);

    final Component header = Component.text()
        .append(border)
        .append(Component.text(" editshop usage ").color(NamedTextColor.YELLOW))
        .append(border)
        .append(Component.newline())
        .build();

    final Component footer = Component.text()
        .content("                                                                        ")
        .decorate(TextDecoration.STRIKETHROUGH)
        .color(NamedTextColor.DARK_GRAY)
        .build();

    final Component info = Component.text(
            "Just look at a sign shop that you own to use this command")
        .color(NamedTextColor.GRAY);

    return Component.text()
        .append(header)
        .append(info)
        .append(Component.newline())

        .append(argUsage("buy", "Makes the shop a buy shop"))
        .append(argUsage("sell", "Makes the shop a sell shop"))

        .append(argUsage("line <2|3>", "Changes the 2nd or 3rd line of the sign"))

        .append(argUsage("amount", "Changes the amount the shop will sell / buy"))
        .append(argUsage("price", "Changes the price of the shop"))
        .append(argUsage("transfer", "Transfers the shop to someone else"))

        .append(footer)
        .build();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Displays help information");

    factory.usage("buy", "Makes the shop you're looking at a buy shop");
    factory.usage("sell", "Makes the shop you're looking at a sell shop");

    factory.usage("line <2 | 3> <text>")
        .addInfo("Changes either the 2nd or 3rd line of the")
        .addInfo("sign shop you're looking at");

    factory.usage("amount <amount: number(1..64)>")
        .addInfo("Changes the amount of items the shop sells/buys");

    factory.usage("price <value: number(1..)>")
        .addInfo("Changes the price of the shop you're looking at");

    factory.usage("transfer <player>")
        .addInfo("Transfers the shop you're looking at to another <player>");

    factory.usage("info")
        .addInfo("Displays info about the shop you're looking at")
        .setPermission(Permissions.ADMIN);

    var data = factory.withPermission(Permissions.ADMIN)
        .withPrefix("data");

    DataCommands.addUsages(data, "Shop", null);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          c.getSource().sendMessage(usageMessage);
          return 0;
        })

        .then(DataCommands.dataAccess("Shop", dataAccess)
            .requires(source -> source.hasPermission(Permissions.ADMIN))
        )

        .then(literal("info")
            .requires(source -> source.hasPermission(Permissions.ADMIN))

            .executes(c -> {
              Player player = c.getSource().asPlayer();
              var shop = getShop(player);

              var writer = TextWriters.newWriter();
              writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

              writer.field("Type", shop.getType().name().toLowerCase());

              writer.field(
                  "Price",
                  Text.format("{0, rhines}", shop.getPrice())
              );

              if (shop.getOwner() != null) {
                writer.field(
                    "Owner",
                    Text.format("{0, user}", shop.getOwner())
                );
              }

              player.sendMessage(writer.asComponent());
              return 0;
            })
        )

        .then(literal("buy").executes(c -> setType(c, false)))
        .then(literal("sell").executes(c -> setType(c, true)))

        .then(literal("price")
            .then(argument("price_actual",
                IntegerArgumentType.integer(0, GeneralConfig.maxSignShopPrice))
                .executes(c -> {
                  Player player = c.getSource().asPlayer();
                  SignShop shop = getShop(player);

                  int price = c.getArgument("price_actual", Integer.class);
                  shop.setPrice(price);

                  player.sendMessage(Messages.shopEditPrice(price));

                  updateShop(shop);
                  return 0;
                })
            )
        )
        .then(literal("amount")
            .then(argument("amount_actual", IntegerArgumentType.integer(1, 64))
                .executes(c -> {
                  Player player = c.getSource().asPlayer();
                  SignShop shop = getShop(player);

                  int amount = c.getArgument("amount_actual", Integer.class);
                  ItemStack exampleItem = shop.getExampleItem();

                  if (amount > exampleItem.getMaxStackSize()) {
                    StringReader reader = new StringReader(c.getInput());
                    reader.setCursor(c.getInput().indexOf(amount + ""));

                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                        .integerTooHigh()
                        .createWithContext(
                            reader, amount,
                            exampleItem.getMaxStackSize()
                        );
                  }

                  exampleItem.setAmount(amount);
                  shop.setExampleItem(exampleItem);

                  player.sendMessage(Messages.shopEditAmount(amount));

                  updateShop(shop);
                  return 0;
                })
            )
        )
        /*.then(literal("transfer")
            .then(argument("player_transfer", Arguments.USER)

                .executes(c -> {
                  User user = getUserSender(c);
                  SignShop shop = getShop(user.getPlayer());
                  User target = Arguments.getUser(c, "player_transfer");

                  if (user.equals(target) && !user.hasPermission(Permissions.SHOP_ADMIN)) {
                    throw Exceptions.TRANSFER_SELF;
                  }

                  shop.setOwner(target.getUniqueId());

                  user.sendMessage(Messages.shopTransferSender(target));
                  target.sendMessage(Messages.shopTransferTarget(user, shop));

                  updateShop(shop);
                  return 0;
                })
            )
        )*/
        .then(literal("line")
            .then(argument("line_actual", IntegerArgumentType.integer(2, 3))
                .suggests((context, builder) -> {
                  return Completions.suggest(builder, "2", "3");
                })

                .then(argument("line_text", Arguments.MESSAGE)
                    .suggests((context, builder) -> {
                      if (builder.getRemainingLowerCase().startsWith("-c")) {
                        builder.suggest("-clear");
                        return builder.buildFuture();
                      }

                      return Arguments.MESSAGE.listSuggestions(context, builder);
                    })

                    .executes(c -> {
                      User user = getUserSender(c);
                      SignShop shop = getShop(user.getPlayer());
                      Sign sign = shop.getSign();

                      int line = c.getArgument("line_actual", Integer.class);
                      var text = Arguments.getMessage(c, "line_text");

                      if (Text.isDashClear(text)) {
                        text = Component.empty();
                      }

                      sign.line(line - 1, text);
                      sign.update();

                      user.sendMessage(Messages.setLine(line, text));

                      updateShop(shop);
                      return 0;
                    })
                )
            )
        );
  }

  private final Component cmdPrefix = Component.text("/" + getName() + " ")
      .color(NamedTextColor.YELLOW);
  private final Component connector = Component.text(" - ").color(NamedTextColor.GRAY);
  private final Style descStyle = Style.style(NamedTextColor.GOLD);

  private Component argUsage(String argument, String usage) {
    return cmdPrefix
        .append(Component.text(argument))
        .append(connector)
        .append(Component.text(usage).style(descStyle))
        .append(Component.newline());
  }

  private int setType(CommandContext<CommandSource> c, boolean sell) throws CommandSyntaxException {
    Player player = c.getSource().asPlayer();
    SignShop shop = getShop(player);

    ShopType type = shop.getType();

    ShopType to = sell ?
        (type.isAdmin() ? ShopType.ADMIN_SELL : ShopType.SELL)
        : (type.isAdmin() ? ShopType.ADMIN_BUY : ShopType.BUY);

    shop.setType(to);

    player.sendMessage(Messages.setShopType(to));

    updateShop(shop);
    return 0;
  }

  private SignShop getShop(Player player) throws CommandSyntaxException {
    Block block = player.getTargetBlockExact(5);

    if (!SignShops.isShop(block)) {
      throw Exceptions.LOOK_AT_SHOP;
    }

    SignShop result = Economy.get().getShops().getShop(block);

    if (!SignShops.mayEdit(result, player.getUniqueId())
        && !player.hasPermission(Permissions.ADMIN)
    ) {
      throw Exceptions.LOOK_AT_SHOP;
    }

    return result;
  }

  private void updateShop(SignShop shop) {
    Tasks.runLater(() -> {
      shop.delayUnload();
      shop.update();
    }, 1);
  }
}