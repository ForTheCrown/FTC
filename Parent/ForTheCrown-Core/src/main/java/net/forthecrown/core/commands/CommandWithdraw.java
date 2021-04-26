package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import org.bukkit.entity.Player;

public class CommandWithdraw extends CrownCommandBuilder {

    public CommandWithdraw(){
        super("withdraw", FtcCore.getInstance());

        maxMoney = FtcCore.getMaxMoneyAmount();

        setUsage("&7Usage:&r /withdraw <amount>");
        setDescription("Used to get cold coins from your balance");
        register();
    }

    private final int maxMoney;

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument("amount", IntegerArgumentType.integer(1, maxMoney))
                .suggests(suggest("1", "5", "10", "50", "100", "500", "1000", "5000"))

                .executes(c -> {
                    Player player = getPlayerSender(c);
                    Balances bals = FtcCore.getBalances();
                    CrownUser user = getUserSender(c);

                    int amount = c.getArgument("amount", Integer.class);

                    if(amount > bals.get(player.getUniqueId())) throw new CrownCommandException("You cannot afford that!");
                    if(player.getInventory().firstEmpty() == -1) throw new CrownCommandException("Your inventory is full! No space for coins!");

                    bals.add(player.getUniqueId(), -amount, false);
                    player.getInventory().addItem(CrownItems.getCoins(amount));
                    user.sendMessage("&7You got a coin that's worth &6" + CrownUtils.decimalizeNumber(amount) + " Rhines");
                    return 0;
                })
        );
    }
}
