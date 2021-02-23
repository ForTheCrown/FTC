package net.forthecrown.core.commands;

import net.forthecrown.core.CrownItems;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WithdrawCommand extends CrownCommand implements TabCompleter{

    public WithdrawCommand(){
        super("withdraw", FtcCore.getInstance());

        setTabCompleter(this);
        setUsage("&7Usage:&r /withdraw <amount>");
        setDescription("Used to get cold coins from your balance");
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        if(args.length != 1) throw new InvalidCommandExecution(sender, CrownUtils.translateHexCodes(getUsage())); // return false just doesn't work wtf

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        }catch (Exception e) { throw new InvalidArgumentException(sender); }

        if(amount <= 0) throw new InvalidArgumentException(sender, "The amount cannot be negative or zero!");

        Player player = (Player) sender;
        Balances bals = FtcCore.getBalances();
        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if(amount > bals.getBalance(player.getUniqueId())) throw new CannotAffordTransaction(player);
        if(player.getInventory().firstEmpty() == -1) throw new InvalidCommandExecution(player, "&cYour inventory is full! &7No space for the coin");

        bals.setBalance(player.getUniqueId(), bals.getBalance(player.getUniqueId()) - amount);
        player.getInventory().setItem(player.getInventory().firstEmpty(), CrownItems.getCoins(amount));
        user.sendMessage("&7You got a coin that's worth &6" + amount + " Rhines");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();

        argList.add("1");
        argList.add("5");
        argList.add("10");
        argList.add("50");
        argList.add("100");
        argList.add("500");
        argList.add("1000");
        argList.add("5000");

        return argList;
    }
}
