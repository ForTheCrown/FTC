package ftc.cosmetics.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class AddGems implements CommandExecutor {

	/*
	 * ----------------------------------------
	 * 			Command description:
	 * ----------------------------------------
	 * Adds gems, make <amount> negative to remove gems.
	 * 
	 * 
	 * Valid usages of command:
	 * - /addgems <player> <amount>
	 * 
	 * Referenced other classes:
	 * - Main: Main.plugin
	 * - DataPlugin: configfile
	 * 
	 * Author: Wout
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Sender must be opped:
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}
		
		// Valid use of command:
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "/addgems [player] [amount]");
			return false;
		}
		
		int amount = 0;
		
		try {
			amount = Integer.parseInt(args[1]);
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "/addgems [player] [amount]");
			return false;
		}
		
		String targetUuid;
		try {
			targetUuid = Bukkit.getPlayer(args[0]).getUniqueId().toString();
		}
		catch (Exception e) {
			try {
				targetUuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
			}
			catch (Exception e2) {
				sender.sendMessage(ChatColor.GRAY + args[0] + " is not a valid player.");
				return false;
			}
		}
		if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getConfigurationSection("players").getKeys(false).contains(targetUuid)) {
			sender.sendMessage(ChatColor.GRAY + args[0] + " not found in dataplugin config.");
			return false;
		}
		// Found matching uuid but names do not match, update name:
		if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + targetUuid + ".PlayerName").equalsIgnoreCase(args[0]))
		{
			Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".PlayerName", args[0]);
		}
		
		// Add gems
		int gems = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getInt("players." + targetUuid + ".Gems");
		gems += amount;
		Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + targetUuid + ".Gems", gems);
		Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
		
		sender.sendMessage(args[0] + ChatColor.GRAY + " now has " + gems + " gems.");
		
		return true;
	}
}
