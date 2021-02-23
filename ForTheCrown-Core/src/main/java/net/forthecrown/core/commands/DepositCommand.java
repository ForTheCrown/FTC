package net.forthecrown.core.commands;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class DepositCommand extends CrownCommand  {

    public DepositCommand(){
        super("deposit", FtcCore.getInstance());

        setUsage("&7Usage: &r/deposit");
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args)  {
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
        bals.addBalance(player.getUniqueId(), amount, false);
        player.sendMessage(CrownUtils.translateHexCodes("&7You deposited " + mainItem.getAmount() + " coins and received &6" + amount + " Rhines"));
        player.getInventory().removeItem(mainItem);
        return true;
    }
}
