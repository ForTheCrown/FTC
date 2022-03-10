package net.forthecrown.commands.economy;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import javax.annotation.Nonnegative;

public class CommandWithdraw extends FtcCommand {

    public CommandWithdraw(){
        super("withdraw", Crown.inst());

        setDescription("Get cold coins from your balance");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("amount", IntegerArgumentType.integer(1, FtcVars.maxMoneyAmount.get()))
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

        if(!economy.has(user.getUniqueId(), totalAmount)) throw FtcExceptionProvider.cannotAfford(totalAmount);
        if(user.getPlayer().getInventory().firstEmpty() == -1) throw FtcExceptionProvider.inventoryFull();

        Component text = Component.translatable("economy.withdraw.total", FtcFormatter.rhines(totalAmount).color(NamedTextColor.YELLOW))
                .color(NamedTextColor.GRAY);

        Component message = Component.translatable("economy.withdraw",
                Component.text(itemAmount + " coin" + FtcUtils.addAnS(itemAmount)),
                FtcFormatter.rhines(amount).color(NamedTextColor.GOLD)
        )
                .color(NamedTextColor.GRAY)
                .append(itemAmount > 1 ? Component.space().append(text) : Component.empty());

        economy.remove(user.getUniqueId(), totalAmount);
        user.getPlayer().getInventory().addItem(ItemStacks.makeCoins(amount, itemAmount));
        user.sendMessage(message);
        return 0;
    }
}
