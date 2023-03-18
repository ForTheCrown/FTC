package net.forthecrown.commands.economy;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.shops.HistoryEntry;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Option;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.page.Footer;
import net.forthecrown.utils.text.format.page.PageFormat;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandShopHistory extends FtcCommand {

  public CommandShopHistory() {
    super("ShopHistory");

    setPermission(Permissions.SHOP_HISTORY);
    setDescription("Shows the history of the shop you're looking at");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /ShopHistory [page] [shop name]
   *
   * Permissions used: ftc.commands.shophistory
   *
   * Main Author: Julie
   */

  public static final ArgumentOption<Integer> PAGE
      = Options.argument(IntegerArgumentType.integer(1))
      .addLabel("page")
      .setDefaultValue(1)
      .build();

  public static final ArgumentOption<Integer> PAGE_SIZE
      = Options.argument(IntegerArgumentType.integer(5, 25))
      .setDefaultValue(10)
      .addLabel("page_size")
      .build();

  public static final ArgumentOption<LocationFileName> SHOP_NAME
      = Options.argument(LocationFileName::parse)
      .addLabel("shop_name")
      .build();

  public static final OptionsArgument ARGS = OptionsArgument.builder()
      .addRequired(PAGE)
      .addOptional(PAGE_SIZE)
      .addOptional(SHOP_NAME)
      .build();

  public static final ContextSet SET = ContextSet.create();
  public static final ContextOption<SignShop> SHOP = SET.newOption();

  public final PageFormat<HistoryEntry> format = Util.make(() -> {
    PageFormat<HistoryEntry> format = PageFormat.create();

    format
        .setHeader(Messages.SHOP_HISTORY_TITLE)
        .setFooter(
            Footer.create()
                .setPageButton((viewerPage, pageSize1, context) -> {
                  return Text.argJoiner(this)
                      .add(SHOP_NAME, context.getOrThrow(SHOP).getName())
                      .add(PAGE, viewerPage)
                      .add(PAGE_SIZE, pageSize1)
                      .joinClickable();
                })
        )

        .setEntry((writer, entry, viewerIndex, context, it) -> {
          writer.write(Messages.formatShopHistory(
              entry,
              context.getOrThrow(SHOP).getExampleItem()
          ));
        });

    return format;
  });

  public static final ParsedOptions EMPTY = new ParsedOptions() {
    @Override
    public @Nullable ParsedOption getParsedOption(@NotNull Option option) {
      return null;
    }

    @Override
    public @Nullable ParsedOption getParsedOption(@NotNull String label) {
      return null;
    }
  };

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> viewPage(c.getSource(), EMPTY))

        .then(argument("args", ARGS)
            .executes(c -> viewPage(c.getSource(), c.getArgument("args", ParsedOptions.class)))
        );
  }

  private int viewPage(CommandSource source, ParsedOptions args)
      throws CommandSyntaxException
  {
    SignShop shop;

    // Get shop, if we were given a shop name
    // get that, else, get the shop the sender is
    // looking at
    if (args.has(SHOP_NAME)) {
      var name = args.getValue(SHOP_NAME);
      var shops = Economy.get().getShops();

      if ((shop = shops.getShop(name)) == null) {
        throw Exceptions.INVALID_SHOP;
      }

      validateShop(shop, source.asBukkit());
    } else {
      shop = get(source.asPlayer());
    }

    // Ensure the history isn't empty
    var history = shop.getHistory();
    if (history.isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    int page = args.getValue(PAGE) - 1;
    int pageSize = args.getValue(PAGE_SIZE);

    // Ensure the page is valid
    Commands.ensurePageValid(page, pageSize, history.size());

    var it = history.pageIterator(page, pageSize);
    var context = SET.createContext().set(SHOP, shop);

    source.sendMessage(format.format(it, context));
    return 0;
  }

  private SignShop get(Player player) throws CommandSyntaxException {
    Block block = player.getTargetBlockExact(5);

    if (!SignShops.isShop(block)) {
      throw Exceptions.LOOK_AT_SHOP;
    }

    SignShop shop = Economy.get()
        .getShops()
        .getShop(WorldVec3i.of(block));

    validateShop(shop, player);
    return shop;
  }

  private void validateShop(SignShop shop, CommandSender sender) throws CommandSyntaxException {
    if (!(sender instanceof Player player)) {
      return;
    }

    // If allowed to look at shop info or has admin permissions
    if (!SignShops.mayEdit(shop, player.getUniqueId())
        && !player.hasPermission(Permissions.SHOP_ADMIN)
    ) {
      throw Exceptions.LOOK_AT_SHOP;
    }
  }
}