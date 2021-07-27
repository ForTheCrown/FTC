package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.economy.Balances;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import javax.annotation.Nonnegative;

public class CommandWithdraw extends FtcCommand {

    public CommandWithdraw(){
        super("withdraw", CrownCore.inst());

        maxMoney = CrownCore.getMaxMoneyAmount();

        setDescription("Get cold coins from your balance");
        register();
    }

    private final Balances bals = CrownCore.getBalances();
    private final int maxMoney;

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("amount", IntegerArgumentType.integer(1, maxMoney))
                .suggests(suggestMonies())

                .then(argument("coinAmount", IntegerArgumentType.integer(1, Material.SUNFLOWER.getMaxStackSize()))
                        .executes(c -> giveCoins(c, c.getArgument("coinAmount", Integer.class)))
                )

                .executes(c -> giveCoins(c, 1))
        );
    }

    private int giveCoins(CommandContext<CommandSource> c, @Nonnegative int itemAmount) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);
        int amount = c.getArgument("amount", Integer.class);
        int totalAmount = amount * itemAmount;

        if(totalAmount > bals.get(user.getUniqueId())) throw FtcExceptionProvider.cannotAfford(totalAmount);
        if(user.getPlayer().getInventory().firstEmpty() == -1) throw FtcExceptionProvider.inventoryFull();

        Component text = Component.translatable("economy.withdraw.total", Balances.formatted(totalAmount).color(NamedTextColor.YELLOW))
                .color(NamedTextColor.GRAY);

        Component message = Component.translatable("economy.withdraw",
                Component.text(itemAmount + " coin" + FtcUtils.addAnS(itemAmount)),
                Balances.formatted(amount).color(NamedTextColor.GOLD)
        )
                .color(NamedTextColor.GRAY)
                .append(itemAmount > 1 ? Component.space().append(text) : Component.empty());

        bals.add(user.getUniqueId(), -totalAmount, false);
        user.getPlayer().getInventory().addItem(CrownItems.makeCoins(amount, itemAmount));
        user.sendMessage(message);
        return 0;
    }
}
