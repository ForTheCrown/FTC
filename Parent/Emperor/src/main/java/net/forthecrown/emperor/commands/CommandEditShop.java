package net.forthecrown.emperor.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.economy.shops.ShopType;
import net.forthecrown.emperor.economy.shops.SignShop;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.ChatUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandEditShop extends CrownCommandBuilder {

    public CommandEditShop(){
        super("editshop", CrownCore.inst());

        maxMoney = CrownCore.getMaxMoneyAmount();

        this.usageMessage = makeUsageMessage();

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
    private final int maxMoney;
    private static final SimpleCommandExceptionType EXCEPTION = new SimpleCommandExceptionType(() -> ChatFormatter.translateHexCodes("&7You must be looking at a sign shop you own"));

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage(usageMessage);
                    return 0;
                })

                .then(argument("buy").executes(c -> setType(c, false)))
                .then(argument("sell").executes(c -> setType(c, true)))

                .then(argument("price")
                        .then(argument("price_actual", IntegerArgumentType.integer(0, maxMoney/2))
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SignShop shop = getShop(player);

                                    int price = c.getArgument("price_actual", Integer.class);
                                    shop.setPrice(price);

                                    player.sendMessage(
                                            Component.text("Shop price set to: ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Balances.formatted(price))
                                                    .append(Component.text("."))
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(argument("amount")
                        .then(argument("amount_actual", IntegerArgumentType.integer(1))
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
                                            Component.text("Shop item amount set to " + amount + ".")
                                                    .color(NamedTextColor.GRAY)
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(argument("transfer")
                        .then(argument("player_transfer", UserType.USER)

                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    SignShop shop = getShop(user.getPlayer());
                                    CrownUser transferTo = UserType.getUser(c, "player_transfer");

                                    if(user.equals(transferTo)) throw FtcExceptionProvider.create("Cannot transfer to yourself");

                                    shop.setOwner(transferTo.getUniqueId());

                                    user.sendMessage(
                                            Component.text("Shop transferred to ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(transferTo.nickDisplayName().color(NamedTextColor.YELLOW))
                                    );

                                    transferTo.sendMessage(
                                            Component.text()
                                                    .color(NamedTextColor.GRAY)
                                                    .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                                                    .append(Component.text(" has transferred a shop to you"))
                                                    .append(Component.newline())
                                                    .append(Component.text("Located at: ")
                                                            .append(ChatFormatter.prettyLocationMessage(shop.getLocation(), false)
                                                                    .color(NamedTextColor.GOLD)
                                                            )
                                                    )
                                                    .build()
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(argument("line")
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
                                                    Component.text("Line " + line + " set to: ")
                                                            .color(NamedTextColor.GRAY)
                                                            .append(empty ? Component.text("empty") : component.color(NamedTextColor.WHITE))
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
        ShopType to = sell ? (type.isAdmin ? ShopType.ADMIN_SELL_SHOP : ShopType.SELL_SHOP) : (type.isAdmin ? ShopType.ADMIN_BUY_SHOP : ShopType.BUY_SHOP);
        shop.setType(to);

        player.sendMessage(
                Component.text("This shop is now a ")
                        .color(NamedTextColor.GRAY)
                        .append(to.inStockLabel())
                        .append(Component.text(" shop."))
        );

        updateShop(shop);
        return 0;
    }

    private SignShop getShop(Player player) throws CommandSyntaxException {
        Block block = player.getTargetBlock(5);
        if(block == null || !(block.getState() instanceof Sign)) throw EXCEPTION.create();

        SignShop result = CrownCore.getShopManager().getShop(block.getLocation());
        if(result == null || !result.getOwner().equals(player.getUniqueId()) && !player.hasPermission("ftc.admin")) throw EXCEPTION.create();
        return result;
    }

    private void updateShop(SignShop shop){
        new BukkitRunnable() {
            @Override
            public void run() {
                shop.update();
            }
        }.runTaskLater(CrownCore.inst(), 1);
    }
}
