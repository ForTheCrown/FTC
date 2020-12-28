package me.wout.DataPlugin.commands;

import java.util.List;

import me.wout.DataPlugin.FtcDataMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class listrank implements CommandExecutor {

	private FtcDataMain plugin;

	public listrank(FtcDataMain plugin)
	{
		plugin.getCommand("listrank").setExecutor(this);
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
			sender.sendMessage(ChatColor.RED + "/listrank [player]");
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
		
		// No ranks to show
		List<String> temp2 = plugin.getConfig().getStringList("players." + playeruuid + ".EarnedRanks");
		if (temp2.isEmpty()) 
		{
			sender.sendMessage(ChatColor.GRAY + "This player does not have any ranks stored in the config.");
			return true;
		}
		
		// List ranks
		String message = ChatColor.GRAY + "";
		for (String rank : temp2) {
			message += rank + " ";
		}
		sender.sendMessage(ChatColor.GRAY + "Ranks of " + args[0] + ":");
		sender.sendMessage(message);
		return true;
	}

}
