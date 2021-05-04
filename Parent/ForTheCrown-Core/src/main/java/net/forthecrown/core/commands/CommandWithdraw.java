package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import javax.annotation.Nonnegative;

public class CommandWithdraw extends CrownCommandBuilder {

    public CommandWithdraw(){
        super("withdraw", FtcCore.getInstance());

        maxMoney = FtcCore.getMaxMoneyAmount();

        setDescription("Used to get cold coins from your balance");
        register();
    }

    private final Balances bals = FtcCore.getBalances();
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

        if(totalAmount > bals.get(user.getUniqueId())) throw FtcExceptionProvider.CANNOT_AFFORD_TRANSACTION.create(Balances.getFormatted(amount));
        if(user.getPlayer().getInventory().firstEmpty() == -1) throw FtcExceptionProvider.create("Your inventory is full! No space for coins!");

        Component text = Component.text(". Total value: ")
                .color(NamedTextColor.GRAY)
                .append(Balances.formatted(totalAmount).color(NamedTextColor.YELLOW));

        bals.add(user.getUniqueId(), -totalAmount, false);
        user.getPlayer().getInventory().addItem(CrownItems.getCoins(amount, itemAmount));
        user.sendMessage(
                Component.text("You got " + itemAmount + " coin" + (itemAmount > 1 ? "s" : "") + " that's worth ")
                        .color(NamedTextColor.GRAY)
                        .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                        .append(itemAmount > 1 ? text : Component.empty())
        );
        return 0;
    }
}
