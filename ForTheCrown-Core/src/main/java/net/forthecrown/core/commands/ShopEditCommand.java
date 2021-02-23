package net.forthecrown.core.commands;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.exceptions.BrokenShopException;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShopEditCommand extends CrownCommand implements TabCompleter {

    public ShopEditCommand(){
        super("editshop", FtcCore.getInstance());
        setUsage("&7Usage: &r/editshop <price | line1 | line2 | sellamount> <value>");
        setTabCompleter(this);
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;

        if(!(player.getTargetBlock(5).getState() instanceof Sign)) throw new InvalidCommandExecution(sender, "&cYou must be looking at a sign shop!");
        Sign sign = (Sign) player.getTargetBlock(5).getState();

        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=") && !sign.getLine(3).contains(ChatColor.GRAY + "Price: "))
            throw new InvalidCommandExecution(sender, "You must be looking at a sign shop!");

        SignShop shop;
        try {
            shop = FtcCore.getShop(sign.getLocation());
        } catch (Exception e){
            e.printStackTrace();
            throw new InvalidCommandExecution(sender, "You must be looking at a sign shop!");
        }

        if(!shop.getOwner().equals(player.getUniqueId()) && !player.hasPermission("ftc.admin")) throw new InvalidCommandExecution(sender, "&cYou must be the owner of the shop!");
        if(args.length < 1)  return false;

        CrownUser user = FtcCore.getUser(player.getUniqueId());

        switch (args[0]){
            case "line1":
                if(args.length == 1) sign.setLine(1, "");
                else {
                 String toSet = String.join(" ", args).replace("line1 ", "");

                 if(player.hasPermission("ftc.donator2")) toSet = CrownUtils.translateHexCodes(toSet);
                 if(player.hasPermission("ftc.donator3")) toSet = CrownUtils.formatEmojis(toSet);

                 sign.setLine(1, toSet);
                }
                user.sendMessage(ChatColor.GREEN + "First line changed!");
                break;

            case "line2":
                if(args.length == 1) sign.setLine(2, "");
                else {
                    String toSet = String.join(" ", args).replace("line2 ", "");

                    if(player.hasPermission("ftc.donator2")) toSet = CrownUtils.translateHexCodes(toSet);
                    if(player.hasPermission("ftc.donator3")) toSet = CrownUtils.formatEmojis(toSet);

                    sign.setLine(2, toSet);
                }
                user.sendMessage(ChatColor.GREEN + "Second line changed!");
                break;

            case "tradeamount":
                if(args.length != 2) throw new InvalidArgumentException(sender, "You must specify an amount");
                if(shop.getInventory().getExampleItem() == null) throw new BrokenShopException(player);

                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception e){ throw new InvalidArgumentException(sender, "&7The amount must be number"); }

                if(amount <= 0 || amount > 64) throw new InvalidArgumentException(sender, "&7The number cannot be zero or less, and cannot be larger than 64");

                ItemStack item = shop.getInventory().getExampleItem();
                item.setAmount(amount);
                shop.getInventory().setExampleItem(item);
                user.sendMessage("&7This shop will now trade items in &e" + amount + " item amounts");
                return true;

            case "price":
                if(args.length != 2) throw new InvalidArgumentException(sender, "You must specify a price");

                int newPrice;
                try {
                    newPrice = Integer.parseInt(args[1]);
                } catch (Exception e){ throw new InvalidArgumentException(sender, args[1] + " is not a number!"); }

                shop.setPrice(newPrice, true);
                user.sendMessage(ChatColor.GREEN + "Price changed! &7The price of this shop is now " + ChatColor.YELLOW + newPrice + " Rhines&7!");
                break;

            default: return false;
        }
        sign.update();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> toReturn = new ArrayList<>();

        if(args.length == 1){
            toReturn.add("line1");
            toReturn.add("line2");
            toReturn.add("tradeamount");
            toReturn.add("price");
            return StringUtil.copyPartialMatches(args[0], toReturn, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
