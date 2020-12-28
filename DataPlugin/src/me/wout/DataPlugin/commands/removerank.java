package me.wout.DataPlugin.commands;

import java.util.ArrayList;
import java.util.List;

import me.wout.DataPlugin.FtcDataMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class removerank implements CommandExecutor {

	private FtcDataMain plugin;

	public removerank(FtcDataMain plugin)
	{
		plugin.getCommand("removerank").setExecutor(this);
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
			sender.sendMessage(ChatColor.RED + "/removerank [player] [rank]");
			return false;
		}
		
		// Valid rank
		List<String> acceptedRanks = new ArrayList<String>();
		String[] temp = {"knight", "baron", "sailor", "pirate"};
		for (String rank : temp)
			acceptedRanks.add(rank);
		if (!acceptedRanks.contains(args[1])) 
		{
			sender.sendMessage(ChatColor.RED + "Invalid rank, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String rank : temp)
				message += rank + ", ";
			sender.sendMessage(message);
			return false;
		}
		acceptedRanks = null;
		temp = null;
		
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
			sender.sendMessage(ChatColor.GRAY + "This player didn't have their information in the config yet.");
			return false;
		}
		
		// Remove rank
		if (args[1].contains("knight") || args[1].contains("baron"))
		{
			List<String> temp2 = plugin.getConfig().getStringList("players." + playeruuid + ".KnightRanks");
			if (!temp2.contains(args[1]))
			{
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " doesn't have the " + args[1] + " rank.");
				return false;
			}
			temp2.remove(args[1]);
			plugin.getConfig().set("players." + playeruuid + ".KnightRanks", temp2);
		}
		else if (args[1].contains("sailor") || args[1].contains("pirate"))
		{
			List<String> temp2 = plugin.getConfig().getStringList("players." + playeruuid + ".PirateRanks");
			if (!temp2.contains(args[1]))
			{
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " doesn't have the " + args[1] + " rank.");
				return false;
			}
			temp2.remove(args[1]);
			plugin.getConfig().set("players." + playeruuid + ".PirateRanks", temp2);
		}
		
		plugin.saveConfig();
		sender.sendMessage(ChatColor.GRAY + "Removed " + args[1] + " from their rank set.");
		
		plugin.getConfig().set("players." + playeruuid + ".CurrentRank", "default");
		plugin.saveConfig();
		Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "tab player " + args[0] + " tabprefix");
		
		return true;
	}

}
