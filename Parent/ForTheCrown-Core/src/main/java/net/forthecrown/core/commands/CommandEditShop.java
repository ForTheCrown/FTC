package net.forthecrown.core.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.ShopManager;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.enums.ShopType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandEditShop extends CrownCommandBuilder {

    public CommandEditShop(){
        super("editshop", FtcCore.getInstance());

        maxMoney = FtcCore.getMaxMoneyAmount();

        setAliases("shopedit", "signshop");
        register();
    }

    private final int maxMoney;
    private static final CrownCommandException EXCEPTION = new CrownCommandException("&7You must be looking at a sign shop you own");

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = getPlayerSender(c);
                    final Component border = Component.text("------------").color(NamedTextColor.DARK_GRAY);

                    final Component header = Component.text()
                            .append(border)
                            .append(Component.text(" editshop usage ").color(NamedTextColor.YELLOW))
                            .append(border)
                            .append(Component.newline())
                            .build();

                    final Component footer = Component.text()
                            .append(Component.text("---------------------------------------"))
                            .color(NamedTextColor.DARK_GRAY)
                            .build();

                    final Component info = Component.text("Just look at a sign shop that you own to use this command").color(NamedTextColor.GRAY);

                    Component mega_message = Component.text()
                            .append(header)
                            .append(info)
                            .append(Component.newline())

                            .append(argUsage("buy", "Makes the shop a buy shop"))
                            .append(argUsage("sell", "Makes the shop a sell shop"))
                            .append(argUsage("amount", "Changes the amount the shop will sell / buy"))
                            .append(argUsage("price", "Changes the price of the shop"))
                            .append(argUsage("transfer", "Transfers the shop to someone else"))

                            .append(footer)
                            .build();

                    player.sendMessage(mega_message);
                    return 0;
                })

                .then(argument("buy").executes(c -> setType(c, false)))
                .then(argument("sell").executes(c -> setType(c, true)))

                .then(argument("price")
                        .then(argument("price_actual", IntegerArgumentType.integer(0, maxMoney/2))
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SignShop shop = getShop(player, true);

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
                                    SignShop shop = getShop(player, true);

                                    int amount = c.getArgument("amount_actual", Integer.class);
                                    ItemStack example_item = shop.getInventory().getExampleItem();

                                    if(amount > example_item.getMaxStackSize()){
                                        StringReader reader = new StringReader(c.getInput());
                                        reader.setCursor(c.getInput().length()-2);
                                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, amount, example_item.getMaxStackSize());
                                    }

                                    example_item.setAmount(amount);
                                    shop.getInventory().setExampleItem(example_item);

                                    player.sendMessage(
                                            Component.text("Shop item amount set to " + amount + ".").color(NamedTextColor.GRAY)
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                )
                .then(argument("transfer")
                        .then(argument("player_transfer", UserType.user())
                                .suggests((c, b) -> UserType.listSuggestions(b))

                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SignShop shop = getShop(player, false);
                                    CrownUser transfer_to = UserType.getUser(c, "player_transfer");

                                    shop.setOwner(transfer_to.getUniqueId());

                                    player.sendMessage(
                                            Component.text("Shop transferred to ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Component.text(transfer_to.getName())
                                                            .color(NamedTextColor.YELLOW)
                                                            .hoverEvent(transfer_to.asHoverEvent())
                                                            .clickEvent(ClickEvent.suggestCommand("/msg " + transfer_to.getName()))
                                                    )
                                    );

                                    updateShop(shop);
                                    return 0;
                                })
                        )
                );
    }

    private final Component cmd_prefix = Component.text("/" + getName() + " ").color(NamedTextColor.YELLOW);
    private final Component connector = Component.text(" - ").color(NamedTextColor.GRAY);
    private final Style desc_style = Style.style(NamedTextColor.GOLD);

    private Component argUsage(String argument, String usage){
        return cmd_prefix
                .append(Component.text(argument))
                .append(connector)
                .append(Component.text(usage).style(desc_style))
                .append(Component.newline());
    }

    private int setType(CommandContext<CommandListenerWrapper> c, boolean sell) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        SignShop shop = getShop(player, true);

        ShopType type = shop.getType();
        ShopType to = sell ? (type.isAdmin() ? ShopType.ADMIN_SELL_SHOP : ShopType.SELL_SHOP) : (type.isAdmin() ? ShopType.ADMIN_BUY_SHOP : ShopType.BUY_SHOP);
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

    private SignShop getShop(Player player, boolean ignoreOwnership) throws CommandSyntaxException {
        Block block = player.getTargetBlock(5);
        if(block == null || !(block.getState() instanceof Sign)) throw EXCEPTION;

        SignShop result = ShopManager.getShop(block.getLocation());
        if(result == null || !result.getOwner().equals(player.getUniqueId()) && !ignoreOwnership) throw EXCEPTION;
        return result;
    }

    private void updateShop(SignShop shop){
        new BukkitRunnable() {
            @Override
            public void run() {
                shop.update();
            }
        }.runTaskLater(FtcCore.getInstance(), 1);
    }
}
