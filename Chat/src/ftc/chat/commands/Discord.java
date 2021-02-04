package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ftc.chat.Main;

public class Discord implements CommandExecutor{
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Sends the sender a discord invitation link.
	 * 
	 * Valid usages of command:
	 * - /discord
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(Main.plugin.getPrefix() + Main.plugin.getDiscord());
		return true;
	}
}
