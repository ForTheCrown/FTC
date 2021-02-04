package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ftc.chat.Main;

public class ReloadConfig implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * If changes were made and saved in the config while the server is running, 
	 * use this to update the plugin without having to reload.
	 * 
	 * Valid usages of command:
	 * - /rlchat
	 * 
	 * Permissions used:
	 * - OP
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Main Author: Wout
	 */
	
	// 
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (sender.isOp()) {
			Main.plugin.reloadConfig();
			sender.sendMessage(ChatColor.GRAY + "Chat-plugin config reloaded.");
			return true;
		} 
		
		else {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
			return false;
		}
	}
}
