package me.wout.DataPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wout.DataPlugin.main;

public class canswapbranch implements CommandExecutor {

	private main plugin;

	public canswapbranch(main plugin) 
	{
		plugin.getCommand("canswapbranch").setExecutor(this);
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
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "/canswapbranch [player]");
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
		
		// Return value
		sender.sendMessage(ChatColor.GRAY + "" + plugin.getConfig().getBoolean("players." + playeruuid + ".CanSwapBranch"));
		return true;
	}

}
