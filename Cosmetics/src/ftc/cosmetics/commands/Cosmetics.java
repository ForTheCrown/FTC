package ftc.cosmetics.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class Cosmetics implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Opens the Cosmetics menu
	 * 
	 * 
	 * Valid usages of command:
	 * - /cosmetics
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
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
		player.openInventory(Main.plugin.getMainCosmeticInventory(player.getUniqueId().toString()));
		
		return true;
	}
}
