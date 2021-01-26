package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.Balances;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DepositCommand implements CrownCommandExecutor {

    public DepositCommand(){
        FtcCore.getInstance().getCommandHandler().registerCommand("deposit", this);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args)  {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;

        if(player.getInventory().getItemInMainHand().getType() != Material.SUNFLOWER
                && (!player.getInventory().getItemInMainHand().hasItemMeta()
                || !player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).contains("Worth ")))
            throw new InvalidCommandExecution(sender, "&cYou need to be holding the coins you wish to deposit");

        ItemStack mainItem = player.getInventory().getItemInMainHand();

        int amount;
        try {
            amount = Integer.parseInt(ChatColor.stripColor(mainItem.getItemMeta().getLore().get(0)).replaceAll("Worth ", "").replaceAll(" Rhines", ""));
        } catch (Exception e) { throw new InvalidCommandExecution(sender, "&cYou need to be holding the coins you wish to deposit"); }

        amount = amount * mainItem.getAmount();

        Balances bals = FtcCore.getBalances();
        bals.addBalance(player.getUniqueId(), amount, true);
        player.sendMessage(FtcCore.translateHexCodes("&7You deposited " + mainItem.getAmount() + " coins and and received &6" + amount + " Rhines"));
        player.getInventory().removeItem(mainItem);
        return true;
    }
}
