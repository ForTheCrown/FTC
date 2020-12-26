package me.wout.DataPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wout.DataPlugin.main;

public class MakeKing implements CommandExecutor {

	private main plugin;

	public MakeKing(main plugin) 
	{
		plugin.getCommand("makeking").setExecutor(this);
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
			sender.sendMessage(ChatColor.RED + "/makeking [player] (king/queen)");
			return false;
		}
		
		// Valid player
		String playeruuid = plugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		
		if (plugin.getConfig().getString("King") != null && (!plugin.getConfig().getString("King").contains("empty")))
		{
			sender.sendMessage(ChatColor.RED + "You have to remove the king first.");
			return false;
		}
		
		// Create section for player if needed
		if ((plugin.getConfig().getConfigurationSection("players") == null) || (!plugin.getConfig().getConfigurationSection("players").getKeys(false).contains(playeruuid))) {
			plugin.createPlayerSection(playeruuid, args[0]);
		}
		
		boolean isKing = true;
		if (args[1].contains("queen")) isKing = false;
		
		if (isKing) 
		{
			Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "tab player " + args[0] + " tabprefix &l[&e&lKing&r&l] &r");
			sender.sendMessage(ChatColor.GRAY + "Made them King");
		}
		else 
		{
			Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "tab player " + args[0] + " tabprefix &l[&e&lQueen&r&l] &r");
			sender.sendMessage(ChatColor.GRAY + "Made them Queen");
		}
		plugin.getConfig().set("King", playeruuid);
		plugin.saveConfig();
		
		return true;
	}

}
