package me.wout.DataPlugin.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wout.DataPlugin.main;

public class setbranch implements CommandExecutor {

	private main plugin;

	public setbranch(main plugin) 
	{
		plugin.getCommand("setbranch").setExecutor(this);
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
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "/setbranch [player] [empty/Knight/Pirate/Viking]");
			return false;
		}
		
		// Valid player
		String playeruuid = plugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		
		// Valid branch
		List<String> acceptedBranches = plugin.getPossibleBranches();
		if (!acceptedBranches.contains(args[1])) 
		{
			sender.sendMessage(ChatColor.RED + "Invalid branch, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String branch : acceptedBranches)
				message += branch + ", ";
			sender.sendMessage(message);
			return false;
		}
		
		// Create section for player if needed
		if ((plugin.getConfig().getConfigurationSection("players") == null) || (!plugin.getConfig().getConfigurationSection("players").getKeys(false).contains(playeruuid))) {
			plugin.createPlayerSection(playeruuid, args[0]);
		}
		
		// Check if CanSwapBranch allows changing
		if (plugin.getConfig().getBoolean("players." + playeruuid + ".CanSwapBranch") == false)
		{
			sender.sendMessage(ChatColor.GRAY + "This player can't swap branches right now because CanSwapBranch is false.");
			return false;
		}
		
		// Change branch
		String oldBranch = plugin.getConfig().getString("players." + playeruuid + ".ActiveBranch");
		plugin.getConfig().set("players." + playeruuid + ".ActiveBranch", args[1]);
		plugin.saveConfig();
		sender.sendMessage(ChatColor.GRAY + "Changed the Active Branch of " + args[0] + " from " + oldBranch + " to " + args[1]);
		return true;
	}

}
