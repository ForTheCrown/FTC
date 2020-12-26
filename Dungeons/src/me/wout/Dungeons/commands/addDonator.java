package me.wout.Dungeons.commands;
import org.bukkit.command.CommandExecutor;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wout.Dungeons.main;
public class addDonator implements CommandExecutor{
	
	private main plugin;
	
	public addDonator(main plugin) {
		this.plugin = plugin;
		plugin.getCommand("rankadd").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "Only opped players can do this.");
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Invalid use of command: " + ChatColor.WHITE + "rankadd [player]");
			return false;
		}
		
		try {
			UUID playerID = Bukkit.getPlayer(args[0]).getUniqueId();
			if (plugin.getConfig().getStringList("Donators").contains(playerID.toString())) {
				sender.sendMessage(args[0] + " was already in the donators list.");
				return false;
			}
			List<String> list = plugin.getConfig().getStringList("Donators");
			list.add(playerID.toString());
			plugin.getConfig().set("Donators", list);
		} 
		catch (NullPointerException e) {
			@SuppressWarnings("deprecation")
			UUID playerID = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
			if (plugin.getConfig().getStringList("Donators").contains(playerID.toString())) {
				sender.sendMessage(args[0] + " was already in the donators list.");
				return false;
			}
			List<String> list = plugin.getConfig().getStringList("Donators");
			list.add(playerID.toString());
			plugin.getConfig().set("Donators", list);
		}
		
		plugin.saveConfig();
		sender.sendMessage(args[0] + " has been added to the donators list.");
		
		return true;
	}
}
