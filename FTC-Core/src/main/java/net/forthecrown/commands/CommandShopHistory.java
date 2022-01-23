package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.shops.ShopHistory;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.ShopOwnership;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.ImmutableCommandExceptionType;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.block.Block;
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

    public static final ImmutableCommandExceptionType INVALID = new ImmutableCommandExceptionType(Component.text("Invalid name"));

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> showPage(c, get(c.getSource().asPlayer()), 1))

                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> showPage(c, get(c.getSource().asPlayer()), c.getArgument("page", Integer.class)))

                        .then(argument("str", StringArgumentType.word())
                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();

                                    LocationFileName name;
                                    try {
                                        name = LocationFileName.parse(c.getArgument("str", String.class));

                                        World world = name.getWorld();
                                        if(world == null) throw INVALID.create();
                                    } catch (IllegalStateException | StringIndexOutOfBoundsException e) {
                                        throw INVALID.create();
                                    }

                                    int page = c.getArgument("page", Integer.class);
                                    SignShop shop = Crown.getShopManager().getShop(name);

                                    ShopOwnership ownership = shop.getOwnership();

                                    // If allowed to look at shop info or has admin permissions
                                    if (!ownership.mayEditShop(player.getUniqueId()) && !player.hasPermission(Permissions.SHOP_ADMIN)) {
                                        throw INVALID.create();
                                    }

                                    return showPage(c, shop, page);
                                })
                        )
                );
    }

    private int showPage(CommandContext<CommandSource> c, SignShop shop, int page) throws CommandSyntaxException {
        Player player = c.getSource().asPlayer();
        ShopHistory history = shop.getHistory();
        page--;

        if(history.isEmpty()) {
            throw FtcExceptionProvider.translatable("shops.history.empty");
        }

        if(!history.isValidPage(page)) {
            throw FtcExceptionProvider.translatable("shops.history.invalidPage", Component.text(page));
        }

        Component display = history.display(page);

        player.sendMessage(display);
        return 0;
    }

    private SignShop get(Player player) throws CommandSyntaxException {
        Block block = player.getTargetBlock(5);
        if(!ShopManager.isShop(block, true)) throw FtcExceptionProvider.translatable("commands.lookingAtShop");

        SignShop shop = Crown.getShopManager().getShop(WorldVec3i.of(block));
        ShopOwnership ownership = shop.getOwnership();

        // If allowed to look at shop info or has admin permissions
        if (!ownership.mayEditShop(player.getUniqueId()) && !player.hasPermission(Permissions.SHOP_ADMIN)) {
            throw FtcExceptionProvider.translatable("commands.lookingAtShop");
        }

        return shop;
    }
}