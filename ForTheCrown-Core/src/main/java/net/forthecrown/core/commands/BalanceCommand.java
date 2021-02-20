package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceCommand extends CrownCommand {

    public BalanceCommand() {
        super("balance", FtcCore.getInstance());

        setUsage("&7Usage: &r/balance <player>");
        setAliases("bal", "bank", "cash", "money");
        setDescription("Displays a player's balance");

        register();
    }



    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Displays either the executors balance or another players balance
     *
     *
     * Valid usages of command:
     * - /balance
     * - /balance <player>
     *
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     * - FtcCore: FtcCore.getOnOffUUID
     *
     * Author: Botul
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        Balances bals = FtcCore.getBalances();

        if(args.length < 1){
            player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + bals.getBalance(player.getUniqueId()) +" Rhines");
            return true;
        }

        UUID targetUUID = FtcCore.getOffOnUUID(args[0]);
        if(targetUUID == null) throw new InvalidPlayerInArgument(sender, args[0]);

        player.sendMessage(ChatColor.GOLD + "$ " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " currently has " + ChatColor.GOLD + bals.getBalance(targetUUID) + " Rhines");
        return true;
    }
}
