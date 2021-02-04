package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help implements CommandExecutor{
	
	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Brings up the help GUI.
	 * 
	 * 
	 * Valid usages of command:
	 * - /help
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * 
	 * Author: Botul
	 * Editor: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only opped players can do this.");
			return false;
		}
		Player player = (Player) sender;


		
		player.sendMessage(ChatColor.GOLD + "Oi! " + ChatColor.GRAY + "This needs some work..");
		return true;
	}
}
