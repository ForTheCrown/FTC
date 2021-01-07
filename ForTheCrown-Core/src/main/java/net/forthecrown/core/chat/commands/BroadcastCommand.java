package net.forthecrown.core.chat.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) return false;
        String message = net.forthecrown.core.files.Messages.getPrefix();
        for (String s : args){
            message += s + " ";
        }
        message = Chat.replaceEmojis(message);
        message = ChatColor.translateAlternateColorCodes('&', message);

        Bukkit.broadcastMessage(message);
        return true;
    }
}
