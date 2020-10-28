package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ftc.chat.Main;

public class Posthelp implements CommandExecutor{

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Displays information about region poles.
	 * 
	 * 
	 * Valid usages of command:
	 * - /posthelp
	 * - /polehelp
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * - Findpole
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
		
		// Send information:
		player.sendMessage(Main.plugin.getPrefix() + ChatColor.YELLOW + "Information about regionpoles:");
		player.sendMessage("You can only teleport between regionpoles.");
		player.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
		player.sendMessage("Use " + ChatColor.YELLOW + "/visit" + ChatColor.RESET + " to travel between them.");
		player.sendMessage("Use " + ChatColor.YELLOW + "/movein" + ChatColor.RESET + " to make a pole your home.");
		player.sendMessage("Then use " + ChatColor.YELLOW + "/home" + ChatColor.RESET + " to go there.");
		
		return true;
	}
}
