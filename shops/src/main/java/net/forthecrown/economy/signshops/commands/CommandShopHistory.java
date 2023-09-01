package net.forthecrown.economy.signshops.commands;

import static net.forthecrown.grenadier.types.options.ParsedOptions.EMPTY;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.signshops.HistoryEntry;
import net.forthecrown.economy.signshops.ShopManager;
import net.forthecrown.economy.signshops.SignShop;
import net.forthecrown.economy.signshops.SignShops;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.page.Footer;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandShopHistory extends FtcCommand {

  public static final ArgumentOption<Integer> PAGE
      = Options.argument(IntegerArgumentType.integer(1))
      .addLabel("page")
      .setDefaultValue(1)
      .build();

  public static final ArgumentOption<Integer> PAGE_SIZE
      = Options.argument(IntegerArgumentType.integer(5, 25))
      .setDefaultValue(10)
      .addLabel("page-size")
      .build();

  public static final ArgumentOption<LocationFileName> SHOP_NAME
      = Options.argument(LocationFileName::parse)
      .addLabel("shop-name")
      .build();

  public static final OptionsArgument ARGS = OptionsArgument.builder()
      .addRequired(PAGE)
      .addOptional(PAGE_SIZE)
      .addOptional(SHOP_NAME)
      .build();

  public static final ContextSet SET = ContextSet.create();
  public static final ContextOption<SignShop> SHOP = SET.newOption();

  public final PageFormat<HistoryEntry> format;

  private final ShopManager manager;

  public CommandShopHistory(ShopManager manager) {
    super("ShopHistory");

    this.manager = manager;
    this.format = PageFormat.create();

    format.setHeader(EconMessages.SHOP_HISTORY_TITLE)
        .setFooter(
            Footer.create()
                .setPageButton((viewerPage, pageSize1, context) -> {
                  return ClickEvent.runCommand(String.format(
                      "/%s shop-name=%s page=%s page-size=%s",
                      getName(),
                      context.getOrThrow(SHOP).getName(),
                      viewerPage,
                      pageSize1
                  ));
                })
        )

        .setEntry((writer, entry, viewerIndex, context, it) -> {
          writer.write(EconMessages.formatShopHistory(
              entry,
              context.getOrThrow(SHOP).getExampleItem()
          ));
        });

    setPermission(EconPermissions.SHOP_HISTORY);
    setDescription("Shows the history of the shop you're looking at");
    simpleUsages();

    register();
  }

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

      if ((shop = manager.getShop(name)) == null) {
        throw EconExceptions.INVALID_SHOP;
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

    int page = args.getValueOptional(PAGE).orElse(1) - 1;
    int pageSize = args.getValueOptional(PAGE_SIZE).orElse(5);

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
      throw EconExceptions.LOOK_AT_SHOP;
    }

    SignShop shop = manager.getShop(WorldVec3i.of(block));

    validateShop(shop, player);
    return shop;
  }

  private void validateShop(SignShop shop, CommandSender sender) throws CommandSyntaxException {
    if (!(sender instanceof Player player)) {
      return;
    }

    // If allowed to look at shop info or has admin permissions
    if (!SignShops.mayEdit(shop, player.getUniqueId())
        && !player.hasPermission(EconPermissions.SHOP_ADMIN)
    ) {
      throw EconExceptions.LOOK_AT_SHOP;
    }
  }
}