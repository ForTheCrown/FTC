package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.files.Balances;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BecomeBaronCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }
        int baronPrice = FtcCore.getInstance().getConfig().getInt("BaronPrice");
        Player player = (Player) sender;
        FtcUser data = FtcCore.getUserData(player.getUniqueId());
        Balances bals = Economy.getBalances();

        if(data.isBaron()){
            player.sendMessage("You are already a baron!");
            return false;
        }

        if(bals.getBalance(player.getUniqueId()) < baronPrice){
            player.sendMessage("You do not have enough money for baron");
            return false;
        }

        if(args.length == 1 && args[0].contains("confirm")){
            bals.setBalance(player.getUniqueId(), bals.getBalance(player.getUniqueId()) - baronPrice);
            data.setBaron(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Congratulations!&r You are now a &ebaron&r!"));
            System.out.println(player.getName() + " became a baron!");
            return true;
        }
        if(args.length == 1) return true;

        TextComponent confirmBaron = new TextComponent(ChatColor.GREEN + "[Confirm]");
        confirmBaron.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/becomebaron confirm"));
        confirmBaron.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Become a baron.")));

        TextComponent baronConfirmMessage = new TextComponent(FtcCore.getPrefix() + ChatColor.translateAlternateColorCodes('&', "&rAre you sure you wish to become a &6baron&r? This will cost &e500,000 &rRhines "));
        baronConfirmMessage.addExtra(confirmBaron);

        player.spigot().sendMessage(baronConfirmMessage);
        return true;
    }
}
