package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.SignShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopEditCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return true;
        }
        Player player = (Player) sender;

        if(!(player.getTargetBlock(5).getState() instanceof Sign)){
            player.sendMessage("You need to be looking at a sign!");
            return true;
        }
        Sign sign = (Sign) player.getTargetBlock(5).getState();

        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=") && !sign.getLine(3).contains(ChatColor.GRAY + "Price: ")){
            player.sendMessage("You need to be looking at a sign shop!");
            return true;
        }

        SignShop shop;
        try {
            shop = FtcCore.getSignShop(sign.getLocation());
        } catch (NullPointerException e){
            e.printStackTrace();
            return true;
        }

        if(shop.getOwner() != player.getUniqueId() && !player.hasPermission("ftc.admin")){
            player.sendMessage("You need to be the owner of the shop!");
            return true;
        }

        if(args.length < 1) return false;

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
                if(args.length != 2){
                    player.sendMessage("You must specify a price!");
                    return true;
                }

                int newPrice;
                try {
                    newPrice = Integer.parseInt(args[1]);
                } catch (Exception e){
                    player.sendMessage("You must specify a number");
                    return true;
                }

                shop.setPrice(newPrice);
                shop.save();
                break;

            default: return false;
        }
        return true;
    }
}
