package me.wout.DataPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wout.DataPlugin.main;

public class setswapbranch implements CommandExecutor {

	private main plugin;

	public setswapbranch(main plugin) 
	{
		plugin.getCommand("setswapbranch").setExecutor(this);
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{	
		// Permission
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return false;
		}

		// Valid use of command
		if ((args.length != 2) || (!(args[1].matches("true") || args[1].matches("false")))) {
			sender.sendMessage(ChatColor.RED + "/setswapbranch [player] [true/false]");
			return false;
		}
		
		// Valid player
		String playeruuid = plugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		
		// Create section for player if needed
		if ((plugin.getConfig().getConfigurationSection("players") == null) || (!plugin.getConfig().getConfigurationSection("players").getKeys(false).contains(playeruuid))) {
			plugin.createPlayerSection(playeruuid, args[0]);
		}
		
		// Set value
		if (args[1].matches("true"))
			plugin.getConfig().set("players." + playeruuid + ".CanSwapBranch", true);
		else
			plugin.getConfig().set("players." + playeruuid + ".CanSwapBranch", false);
		plugin.saveConfig();
		sender.sendMessage(ChatColor.GRAY + "Set the value of canswapbranch of " + args[0] + " to " + args[1]);
		return true;
	}

}
