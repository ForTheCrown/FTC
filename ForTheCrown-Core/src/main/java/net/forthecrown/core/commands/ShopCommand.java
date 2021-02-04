package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.inventories.SellShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopCommand extends CrownCommand implements TabCompleter {

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

    public ShopCommand(){
        super("shop", FtcCore.getInstance());

        setUsage("&7Usage:&r /shop [mining | farming | drops]");
        setDescription("Opens the Shop GUI in which one can sell things");
        setTabCompleter(this);
        register();
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        SellShop sellShop = new SellShop(player);

        if(args.length == 0) player.openInventory(sellShop.mainMenu());
        else {
         switch (args[0]){
             default: throw new InvalidArgumentException(sender);

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = Arrays.asList("drops", "farming", "mining", "web");
        if(args.length == 1) return StringUtil.copyPartialMatches(args[0], argList, new ArrayList<>());

        return null;
    }
}
