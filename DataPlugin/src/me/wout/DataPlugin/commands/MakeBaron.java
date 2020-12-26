package me.wout.DataPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import me.wout.DataPlugin.main;

public class MakeBaron implements CommandExecutor {

	private main plugin;

	public MakeBaron(main plugin) 
	{
		plugin.getCommand("makebaron").setExecutor(this);
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
			sender.sendMessage(ChatColor.RED + "/makebaron [player]");
			return false;
		}
		
		// Valid player
		String playeruuid = plugin.trySettingUUID(args[0]);
		if (playeruuid == null)
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid playername.");
			return false;
		}
		
		Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "addrank " + args[0] + " baron");
		sender.sendMessage(ChatColor.GRAY + "Added baron to their rank set.");
		
		Objective baron = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron");
		Score baronPoints = baron.getScore(args[0]);
		baronPoints.setScore(1);
		
		return true;
	}

}
