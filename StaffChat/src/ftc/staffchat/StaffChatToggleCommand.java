package ftc.staffchat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class StaffChatToggleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){sender.sendMessage("Only players can execute this command"); return false;}
        Player player = (Player) sender;

        List<String> playerList = Main.plugin.getConfig().getStringList("PlayersWithSCT");
        if(playerList.contains(player.getUniqueId().toString())){ //if they're in the list remove em, if they're not, add em yada yada
            playerList.remove(player.getUniqueId().toString());
            player.sendMessage(ChatColor.GRAY + "All your message will no longer go to staff chat");
        } else {
            playerList.add(player.getUniqueId().toString());
            player.sendMessage(ChatColor.GRAY + "All your messages will now go to staff chat");
        }
        Main.plugin.getConfig().set("PlayersWithSCT", playerList);
        Main.plugin.saveConfig();

        return true;
    }
}
