package ftc.chat.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

//import ftc.chat.Main;

public class _TEMPLATE implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Explain what command is supposed to be used for..
	 * 
	 * 
	 * Valid usages of command:
	 * - /mycommand arg1
	 * - /mycommand arg1 arg2 ...
	 * 
	 * Permissions used:
	 * - OP
	 * - ...
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * - ...
	 * 
	 * Author: Wout
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Sender must be player:
		/*if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}*/
		
		// Sender must be opped:
		/*if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}*/
		
		// Command args:
		/*if(args.length < 1) {
        	sender.sendMessage(ChatColor.GRAY + "Usage: /mycommand <arg1>");
        	return false;
        }*/
		
		
		// Player player = (Player) sender;
		// Location playerloc = player.getLocation();
		
		return true;
	}
}
