package me.wout.DataPlugin.commands;

import java.util.ArrayList;
import java.util.List;

import me.wout.DataPlugin.FtcDataMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class addrank implements CommandExecutor {

	private FtcDataMain plugin;

	public addrank(FtcDataMain plugin)
	{
		plugin.getCommand("addrank").setExecutor(this);
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
			sender.sendMessage(ChatColor.RED + "/addrank [player] [rank]");
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
		}
		
		// Add rank
		if (args[1].contains("knight") || args[1].contains("baron"))
		{
			List<String> temp2 = plugin.getConfig().getStringList("players." + playeruuid + ".KnightRanks");
			if (temp2.contains(args[1]))
			{
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " already has the " + args[1] + " rank.");
				return false;
			}
			temp2.add(args[1]);
			plugin.getConfig().set("players." + playeruuid + ".KnightRanks", temp2);
		}
		else if (args[1].contains("sailor") || args[1].contains("pirate"))
		{
			List<String> temp2 = plugin.getConfig().getStringList("players." + playeruuid + ".PirateRanks");
			if (temp2.contains(args[1]))
			{
				sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " already has the " + args[1] + " rank.");
				return false;
			}
			temp2.add(args[1]);
			plugin.getConfig().set("players." + playeruuid + ".PirateRanks", temp2);
		}
		
		plugin.saveConfig();
		sender.sendMessage(ChatColor.GRAY + "Added " + args[1] + " to their rank set.");
		return true;
	}

}
