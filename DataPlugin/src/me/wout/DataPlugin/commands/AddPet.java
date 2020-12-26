package me.wout.DataPlugin.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wout.DataPlugin.main;

public class AddPet implements CommandExecutor {

	private main plugin;

	public AddPet(main plugin) 
	{
		plugin.getCommand("addpet").setExecutor(this);
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
			sender.sendMessage(ChatColor.RED + "/addpet [player] [pet]");
			return false;
		}
		
		// Valid pet
		List<String> acceptedPets = new ArrayList<String>();
		String[] temp = {"gray_parrot", "green_parrot", "blue_parrot"};
		for (String rank : temp)
			acceptedPets.add(rank);
		if (!acceptedPets.contains(args[1])) 
		{
			sender.sendMessage(ChatColor.RED + "Invalid pet, use of one these:");
			String message = ChatColor.GRAY + "";
			for (String pet : temp)
				message += pet + ", ";
			sender.sendMessage(message);
			return false;
		}
		acceptedPets = null;
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
		
		// Add pet
		List<String> pets = plugin.getConfig().getStringList("players." + playeruuid + ".Pets");
		if (pets == null) 
		{
			plugin.getConfig().createSection("players." + playeruuid + ".Pets");
			plugin.getConfig().set("players." + playeruuid + ".Pets", new ArrayList<String>());
		}
		else if (pets.contains(args[1]))
		{
			sender.sendMessage(ChatColor.GRAY + args[0].toLowerCase() + " already has the " + args[1] + " pet.");
			return false;
		}
		pets.add(args[1]);
		plugin.getConfig().set("players." + playeruuid + ".Pets", pets);
		
		
		plugin.saveConfig();
		sender.sendMessage(ChatColor.GRAY + "Added " + args[1] + " to their pet list.");
		return true;
	}

}
