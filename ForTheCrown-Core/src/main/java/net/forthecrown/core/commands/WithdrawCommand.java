package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.CrownItems;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.Balances;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WithdrawCommand implements TabCompleter, CrownCommandExecutor {

    public WithdrawCommand(){
        FtcCore.getInstance().getCommandHandler().registerCommand("withdraw", this, this);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        if(args.length != 1) return false;

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        }catch (Exception e) { throw new InvalidArgumentException(sender); }

        if(amount <= 0) throw new InvalidArgumentException(sender, "The amount cannot be negative or zero!");

        Player player = (Player) sender;
        Balances bals = FtcCore.getBalances();
        FtcUser user = FtcCore.getUser(player.getUniqueId());

        if(amount > bals.getBalance(player.getUniqueId())) throw new CannotAffordTransaction(player);
        if(player.getInventory().firstEmpty() == -1) throw new InvalidCommandExecution(player, "&cYour inventory is full! &7No space for the coin");

        bals.setBalance(player.getUniqueId(), bals.getBalance(player.getUniqueId()) - amount);
        player.getInventory().setItem(player.getInventory().firstEmpty(), CrownItems.getCoins(amount));
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
