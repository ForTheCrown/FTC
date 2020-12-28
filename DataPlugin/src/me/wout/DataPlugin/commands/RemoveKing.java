package me.wout.DataPlugin.commands;

import java.util.UUID;

import me.wout.DataPlugin.FtcDataMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RemoveKing implements CommandExecutor {

	private FtcDataMain plugin;

	public RemoveKing(FtcDataMain plugin)
	{
		plugin.getCommand("removeking").setExecutor(this);
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
		if (args.length != 0) {
			sender.sendMessage(ChatColor.RED + "/removeking");
			return false;
		}
		
		// Valid player
		if (plugin.getConfig().getString("King") == null)
		{
			plugin.getConfig().set("King", "FTCempty");
			sender.sendMessage(ChatColor.GRAY + "There is no active king atm.");
			return false;
		}
		else if (plugin.getConfig().getString("King").contains("FTCempty"))
		{
			sender.sendMessage(ChatColor.GRAY + "There is no active king atm.");
			return false;
		}
		
		String playerName = "";
		try {
			playerName = Bukkit.getPlayer(UUID.fromString(plugin.getConfig().getString("King"))).getName();
		} catch (Exception tryoffline) {
			try {
				playerName = Bukkit.getOfflinePlayer(UUID.fromString(plugin.getConfig().getString("King"))).getName();
			}
			catch (Exception noplayer) {
				sender.sendMessage(ChatColor.GRAY + "King uuid in config doesn't link to a player.");
				return false;
			}
		}

		//Bukkit.broadcastMessage("tab player " + playerName + " tabprefix");
		Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "tab player " + playerName + " tabprefix");
		sender.sendMessage(ChatColor.GRAY + "Removed their title.");
		
		plugin.getConfig().set("King", "FTCempty");
		plugin.saveConfig();
		
		return true;
	}

}
