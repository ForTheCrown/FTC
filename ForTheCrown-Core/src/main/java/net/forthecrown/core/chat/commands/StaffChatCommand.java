package net.forthecrown.core.chat.commands;

import net.forthecrown.core.chat.Chat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String message = ChatColor.GRAY + "[Staff] %SENDER%"  + ChatColor.GRAY + ChatColor.BOLD + " >" + ChatColor.RESET + "%MESSAGE%";

        if(args.length < 1) return false;

        String initialMmg = "";
        for (String s : args){ initialMmg += " " + s; }

        initialMmg = ChatColor.translateAlternateColorCodes('&', Chat.replaceEmojis(initialMmg));
        message = message.replaceAll("%MESSAGE%", initialMmg);

        if(sender instanceof Player) message = message.replaceAll("%SENDER%", sender.getName());
        else message = message.replaceAll("%SENDER%", "SERVER");

        System.out.println(message);

        for (Player p : Bukkit.getOnlinePlayers()){
            if(p.hasPermission("ftc.staffchat")) p.sendMessage(message);
        }
        return true;
    }
}
