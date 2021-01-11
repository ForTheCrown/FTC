package net.forthecrown.core.economy.commands;

import net.forthecrown.core.economy.SellShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return true;
        }
        Player player = (Player) sender;
        SellShop sellShop = new SellShop(player);

        if(args.length == 0) player.openInventory(sellShop.mainMenu());
        else {
         switch (args[0]){
             default: return false;

             case "drops":
                 player.openInventory(sellShop.dropsMenu());
                 break;
             case "mining":
                 player.openInventory(sellShop.miningMenu());
                 break;
             case "farming":
                 player.openInventory(sellShop.farmingMenu());
                 break;
             case "web":
                 player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.YELLOW + "You can visit the webshop here:");
                 player.sendMessage(ChatColor.AQUA + "https://for-the-crown.tebex.io/");
                 break;
             }
        }
        return true;
    }
}
