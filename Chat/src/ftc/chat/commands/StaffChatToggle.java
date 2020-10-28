package ftc.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ftc.chat.Main;

import java.util.List;

public class StaffChatToggle implements CommandExecutor {
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Toggles staffchat.
	 * 
	 * 
	 * Valid usages of command:
	 * - /sct
	 * 
	 * Permissions used:
	 * - ftc.staffchat
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Main Author: Botul
	 * Edit by: Wout
	 */
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// Sender must be player:
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		
		Player player = (Player) sender;
		List<String> playerList = Main.plugin.getConfig().getStringList("PlayersWithSCT");
		
		// Toggle sender in config list:
		if (playerList.contains(player.getUniqueId().toString())) { 
		    playerList.remove(player.getUniqueId().toString());
		    player.sendMessage(ChatColor.GRAY + "All your message will no longer go to staff chat.");
		} 
		else {
		    playerList.add(player.getUniqueId().toString());
		    player.sendMessage(ChatColor.GRAY + "All your messages will now go to staff chat.");
		}
		
		// Save config:
		Main.plugin.getConfig().set("PlayersWithSCT", playerList);
		Main.plugin.saveConfig();
		
		return true;
    }
}
