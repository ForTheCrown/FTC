package net.forthecrown.commands.economy;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.shops.HistoryEntry;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.page.Footer;
import net.forthecrown.utils.text.format.page.PageFormat;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandShopHistory extends FtcCommand {

    public CommandShopHistory() {
        super("ShopHistory");

        setPermission(Permissions.SHOP_HISTORY);
        setDescription("Shows a shop's history");

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

    public static final Argument<Integer> PAGE = Argument.of("page", IntegerArgumentType.integer(1), 1);
    public static final Argument<Integer> PAGE_SIZE = Argument.of("page_size", IntegerArgumentType.integer(5, 25), 10);
    public static final Argument<LocationFileName> SHOP_NAME = Argument.of("shop_name", LocationFileName::parse);

    public static final ArgsArgument ARGS = ArgsArgument.builder()
            .addRequired(PAGE)
            .addOptional(PAGE_SIZE)
            .addOptional(SHOP_NAME)
            .build();

    public static final ParsedArgs EMPTY = new ParsedArgs() {
        @Override
        public <T> T getOrDefault(Argument<T> argument, T t) {
            return t;
        }

        @Override
        public <T> T getOrDefault(String s, Class<T> aClass, T t) {
            return t;
        }

        @Override
        public <T> T getOrDefault(String s, Class<T> aClass, T t, CommandSource source) {
            return t;
        }

        @Override
        public int size() {
            return 0;
        }
    };

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> viewPage(c.getSource(), EMPTY))

                .then(argument("args", ARGS)
                        .executes(c -> viewPage(c.getSource(), c.getArgument("args", ParsedArgs.class)))
                );
    }

    private int viewPage(CommandSource source, ParsedArgs args) throws CommandSyntaxException {
        SignShop shop;

        // Get shop, if we were given a shop name
        // get that, else, get the shop the sender is
        // looking at
        if (args.has(SHOP_NAME)) {
            var name = args.get(SHOP_NAME);
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

        int page = args.get(PAGE) - 1;
        int pageSize = args.get(PAGE_SIZE);

        // Ensure the page is valid
        Commands.ensurePageValid(page, pageSize, history.size());

        var joiner = Text.argJoiner(this)
                .add(SHOP_NAME, shop.getName());

        var it = history.pageIterator(page, pageSize);
        PageFormat<HistoryEntry> format = PageFormat.create();

        format
                .setHeader(Messages.SHOP_HISTORY_TITLE)
                .setFooter(
                        Footer.create()
                                .setPageButton((viewerPage, pageSize1) -> {
                                    return joiner
                                            .add(PAGE, viewerPage)
                                            .add(PAGE_SIZE, pageSize1)
                                            .joinClickable();
                                })
                )

                .setEntry((writer, entry, viewerIndex) -> {
                    writer.write(Messages.formatShopHistory(
                                    entry,
                                    shop.getExampleItem()
                    ));
                });

        source.sendMessage(format.format(it));
        return 0;
    }

    private SignShop get(Player player) throws CommandSyntaxException {
        Block block = player.getTargetBlock(5);

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