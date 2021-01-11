package net.forthecrown.core.chat.commands;

import net.forthecrown.core.chat.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class StaffChatToggleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players may execute this command!");
            return false;
        }

        Player player = (Player) sender;
        Set<Player> set = Chat.getSCTPlayers();

        if(set.contains(player)) set.remove(player);
        else set.add(player);

        Chat.setSCTPlayers(set);
        return true;
    }
}
