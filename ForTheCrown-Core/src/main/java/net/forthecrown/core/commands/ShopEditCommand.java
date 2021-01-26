package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.core.files.SignShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ShopEditCommand implements CrownCommandExecutor, TabCompleter {

    public ShopEditCommand(){
        FtcCore.getInstance().getCommandHandler().registerCommand("editshop", this, this);
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;

        if(!(player.getTargetBlock(5).getState() instanceof Sign)) throw new InvalidCommandExecution(sender, "&cYou must be looking at a sign shop!");
        Sign sign = (Sign) player.getTargetBlock(5).getState();

        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=") && !sign.getLine(3).contains(ChatColor.GRAY + "Price: "))
            throw new InvalidCommandExecution(sender, "You must be looking at a sign shop!");

        SignShop shop;
        try {
            shop = FtcCore.getSignShop(sign.getLocation());
        } catch (Exception e){
            e.printStackTrace();
            return true;
        }

        if(!shop.getOwner().equals(player.getUniqueId()) && !player.hasPermission("ftc.admin")) throw new InvalidCommandExecution(sender, "&cYou must be the owner of the shop!");

        if(args.length < 1)  return false;

        switch (args[0]){
            case "line1":
                if(args.length == 1) sign.setLine(1, "");
                else {
                 String toSet = String.join(" ", args).replace("line1 ", "");
                 sign.setLine(1, toSet);
                }
                break;

            case "line2":
                if(args.length == 1) sign.setLine(2, "");
                else {
                    String toSet = String.join(" ", args).replace("line2 ", "");
                    sign.setLine(2, toSet);
                }
                break;

            case "price":
                if(args.length != 2) throw new InvalidArgumentException(sender, "You must specify a price");

                int newPrice;
                try {
                    newPrice = Integer.parseInt(args[1]);
                } catch (Exception e){ throw new InvalidArgumentException(sender, args[1] + " is not a number!"); }

                shop.setPrice(newPrice);
                shop.save();
                break;

            default: return false;
        }
        sign.update();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int argN = args.length-1;
        List<String> toReturn = new ArrayList<>();

        if(args.length == 1){
            toReturn.add("line1");
            toReturn.add("line2");
            toReturn.add("price");
            return StringUtil.copyPartialMatches(args[argN], toReturn, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
