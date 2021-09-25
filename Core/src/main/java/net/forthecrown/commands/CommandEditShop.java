package net.forthecrown.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.Container;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandEditShop extends FtcCommand {

    public CommandEditShop(){
        super("editshop", Crown.inst());

        usageMessage = makeUsageMessage();

        setDescription("Allows you edit a shop");
        setAliases("shopedit", "signshop");
        register();
    }

    private Component makeUsageMessage(){
        final Component border = Component.text("------------").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH);

        final Component header = Component.text()
                .append(border)
                .append(Component.text(" editshop usage ").color(NamedTextColor.YELLOW))
                .append(border)
                .append(Component.newline())
                .build();

        final Component footer = Component.text()
                .append(Component.text("---------------------------------------").decorate(TextDecoration.STRIKETHROUGH))
                .color(NamedTextColor.DARK_GRAY)
                .build();

        final Component info = Component.text("Just look at a sign shop that you own to use this command").color(NamedTextColor.GRAY);

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

    private final Component usageMessage;

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage(usageMessage);
                    return 0;
                })

                .then(literal("buy").executes(c -> setType(c, false)))
                .then(literal("sell").executes(c -> setType(c, true)))

                .then(literal("price")
                        .then(argument("price_actual", IntegerArgumentType.integer(0, ComVars.getMaxSignShopPrice()))
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SignShop shop = getShop(player);

                                    int price = c.getArgument("price_actual", Integer.class);
                                    shop.setPrice(price);

                                    player.sendMessage(
                                            Component.translatable("shops.edit.price", FtcFormatter.rhines(price)).color(NamedTextColor.GRAY)
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(literal("amount")
                        .then(argument("amount_actual", IntegerArgumentType.integer(1, Container.MAX_STACK))
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SignShop shop = getShop(player);

                                    int amount = c.getArgument("amount_actual", Integer.class);
                                    ItemStack exampleItem = shop.getInventory().getExampleItem();

                                    if(amount > exampleItem.getMaxStackSize()){
                                        StringReader reader = new StringReader(c.getInput());
                                        reader.setCursor(c.getInput().indexOf(amount + ""));

                                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, amount, exampleItem.getMaxStackSize());
                                    }

                                    exampleItem.setAmount(amount);
                                    shop.getInventory().setExampleItem(exampleItem);

                                    player.sendMessage(
                                            Component.translatable("shops.edit.amount", Component.text(amount))
                                                    .color(NamedTextColor.GRAY)
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(literal("transfer")
                        .then(argument("player_transfer", UserArgument.user())

                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    SignShop shop = getShop(user.getPlayer());
                                    CrownUser transferTo = UserArgument.getUser(c, "player_transfer");

                                    if(user.equals(transferTo) && !user.hasPermission(Permissions.SHOP_ADMIN)) throw FtcExceptionProvider.translatable("shops.edit.transferToSelf");

                                    shop.setOwner(transferTo.getUniqueId());

                                    user.sendMessage(
                                            Component.translatable("shops.edit.transferred", transferTo.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GRAY)
                                    );

                                    transferTo.sendMessage(
                                            Component.translatable("shops.edit.transferred.receiver",
                                                    user.nickDisplayName().color(NamedTextColor.YELLOW),
                                                    FtcFormatter.prettyLocationMessage(shop.getLocation(), false).color(NamedTextColor.GOLD)
                                            ).color(NamedTextColor.GRAY)
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(literal("line")
                        .then(argument("line_actual", IntegerArgumentType.integer(2, 3))
                                .suggests(suggestMatching("2", "3"))

                                .then(argument("line_text", StringArgumentType.greedyString())
                                        .suggests((c, b) -> {
                                            if(!b.getRemaining().startsWith("-e")) return Suggestions.empty();
                                            return b.suggest("-empty").buildFuture();
                                        })

                                        .executes(c -> {
                                            CrownUser user = getUserSender(c);
                                            SignShop shop = getShop(user.getPlayer());
                                            Sign sign = shop.getSign();

                                            int line = c.getArgument("line_actual", Integer.class);
                                            String lineText = c.getArgument("line_text", String.class);
                                            boolean empty = lineText.equalsIgnoreCase("-empty");
                                            Component component = empty ? Component.empty() : ChatUtils.convertString(lineText, user.hasPermission("ftc.donator2"));

                                            sign.line(line-1, component);
                                            sign.update();

                                            user.sendMessage(
                                                    Component.translatable("shops.edit.line", NamedTextColor.GRAY, Component.text(line), (empty ? Component.text("empty") : component))
                                            );

                                            updateShop(shop);
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private final Component cmdPrefix = Component.text("/" + getName() + " ").color(NamedTextColor.YELLOW);
    private final Component connector = Component.text(" - ").color(NamedTextColor.GRAY);
    private final Style descStyle = Style.style(NamedTextColor.GOLD);

    private Component argUsage(String argument, String usage){
        return cmdPrefix
                .append(Component.text(argument))
                .append(connector)
                .append(Component.text(usage).style(descStyle))
                .append(Component.newline());
    }

    private int setType(CommandContext<CommandSource> c, boolean sell) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        SignShop shop = getShop(player);

        ShopType type = shop.getType();
        ShopType to = sell ? (type.isAdmin() ? ShopType.ADMIN_SELL : ShopType.SELL) : (type.isAdmin() ? ShopType.ADMIN_BUY : ShopType.BUY);
        shop.setType(to);

        player.sendMessage(Component.translatable("shops.edit.type", to.inStockLabel()).color(NamedTextColor.GRAY));

        updateShop(shop);
        return 0;
    }

    private SignShop getShop(Player player) throws CommandSyntaxException {
        Block block = player.getTargetBlock(5);
        if(block == null || !(block.getState() instanceof Sign)) throw FtcExceptionProvider.translatable("commands.lookingAtShop");

        SignShop result = Crown.getShopManager().getShop(block.getLocation());
        if(result == null || !result.getOwner().equals(player.getUniqueId()) && !player.hasPermission("ftc.admin")) throw FtcExceptionProvider.translatable("commands.lookingAtShop");
        return result;
    }

    private void updateShop(SignShop shop){
        new BukkitRunnable() {
            @Override
            public void run() {
                shop.update();
            }
        }.runTaskLater(Crown.inst(), 1);
    }
}
