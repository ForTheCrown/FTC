package net.forthecrown.commands.economy;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.core.Messages;
import net.forthecrown.user.User;
import org.bukkit.Sound;

import javax.annotation.Nonnegative;

public class CommandWithdraw extends FtcCommand {

    public CommandWithdraw(){
        super("withdraw");

        setDescription("Get cold coins from your balance");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("amount", IntegerArgumentType.integer(1))
                .suggests(CommandPay.BAL_SUGGESTIONS)

                .then(argument("coinAmount", IntegerArgumentType.integer(1, FtcItems.COIN_MATERIAL.getMaxStackSize()))
                        .executes(c -> giveCoins(c, c.getArgument("coinAmount", Integer.class)))
                )

                .executes(c -> giveCoins(c, 1))
        );
    }

    private int giveCoins(CommandContext<CommandSource> c, @Nonnegative int itemAmount) throws CommandSyntaxException {
        User user = getUserSender(c);
        int amount = c.getArgument("amount", Integer.class);
        int totalAmount = amount * itemAmount;

        if (!user.hasBalance(totalAmount)) {
            throw Exceptions.cannotAfford(totalAmount);
        }

        var inventory = user.getPlayer().getInventory();

        if (inventory.firstEmpty() == -1) {
            throw Exceptions.INVENTORY_FULL;
        }

        inventory.addItem(FtcItems.makeCoins(amount, itemAmount));

        user.removeBalance(totalAmount);
        user.sendMessage(Messages.withdrew(itemAmount, totalAmount));
        user.playSound(Sound.ENTITY_ITEM_PICKUP, 1, 1);

        Transactions.builder()
                .type(TransactionType.WITHDRAW)
                .sender(user.getUniqueId())
                .extra("amount=%s itemAmount=%s", amount, itemAmount)
                .amount(totalAmount)
                .log();

        return 0;
    }
}