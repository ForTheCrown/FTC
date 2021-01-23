package net.forthecrown.core.commands;

import net.forthecrown.core.inventories.SellShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the ShopGUI and allows players to sell their items
     *
     *
     * Valid usages of command:
     * - /shop
     * - /shop <farming | mining | drops | web>
     *
     * Referenced other classes:
     * - SellShop
     *
     * Author: Botul
     */

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
                 player.performCommand("buy");
                 break;
             }
        }
        return true;
    }
}
