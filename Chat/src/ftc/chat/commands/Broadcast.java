package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ftc.chat.Main;

public class Broadcast implements CommandExecutor{
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Adds the prefix defined in the config to a message
	 * and sends it to all players online.
	 * 
	 * 
	 * Valid usages of command:
	 * (bc is short for broadcast)
	 * - /bc [message]
	 * 
	 * Permissions used:
	 * - OP
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Command args:
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GRAY + "Usage: /bc [message]");
			return false;
		}
		
		// Sender must be opped:
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}
		
		// Construct message and broadcast to all:
		String message = Main.plugin.getPrefix();
		for (String mes : args)
			message += mes + " ";
		
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
		
		return true;
	}
}
