package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.exceptions.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommand extends CrownCommand {

    public PayCommand(){
        super("pay", FtcCore.getInstance());

        setDescription("Pays another player money");
        setUsage("&7Usage: &r/pay <user> <amount>");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Pays another player a set amount of money, removes the money from the player as well
     *
     *
     * Valid usages of command:
     * - /pay <player> <amount>
     *
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     *
     * Author: Botul
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        if(args.length != 2) throw new TooLittleArgumentsException(sender);

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        UUID target;

        if(args[0].contains(player.getName())) throw new InvalidArgumentException(sender, "You can't pay yourself");

        try {
            target = FtcCore.getOffOnUUID(args[0]);
        } catch (Exception e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        int amountToPay;
        try {
            amountToPay = Integer.parseInt(args[1]);
        } catch (Exception e){ throw new InvalidArgumentException(sender, "The amount to pay must be a number!"); }

        if(amountToPay <= 0) throw new InvalidArgumentException(sender, "You can't pay negative amounts");

        Balances bals = FtcCore.getBalances();

        if(bals.getBalance(playerUUID) < amountToPay) throw new CannotAffordTransaction(sender);

        //gives money to target
        bals.addBalance(target, amountToPay);

        //removes money from player
        bals.addBalance(playerUUID, -amountToPay);

        player.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + amountToPay + " Rhines " + ChatColor.GRAY + "to " + ChatColor.YELLOW + args[0]);
        try{
            Bukkit.getPlayer(target).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + amountToPay + " Rhines " + ChatColor.GRAY + "by " + ChatColor.YELLOW + player.getName());
        } catch (Exception e){
            return true;
        }

        return true;
    }
}
