package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ftc.chat.Main;

public class Spawn implements CommandExecutor{

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Explains how to get to spawn.
	 * 
	 * 
	 * Valid usages of command:
	 * - /spawn
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * - Findpost
	 * - Posthelp
	 * 
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Sender must be player:
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		
		Player player = (Player) sender;
		
		// Information:
		player.sendMessage(Main.plugin.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
		player.sendMessage("Spawn is called Hazelguard, you can tp using regionpoles.");
		player.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
		player.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
		player.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");
		
		return true;
	}
}
