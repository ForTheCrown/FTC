package net.forthecrown.core.chat.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Discord implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(FtcCore.getPrefix() + Chat.getDiscord());
        return true;
    }
}
