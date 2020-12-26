package me.wout.Pirate.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.entity.Player;

import me.wout.Pirate.Main;

public class parrot implements CommandExecutor {
	
	public parrot() {
		Main.plugin.getCommand("parrot").setExecutor(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		
		//temp
		/*if (!player.isOp()) 
		{
			player.sendMessage("No perms");
			return false;
		}*/
		
		if (!Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".ActiveBranch").contains("Pirate")) {
			player.sendMessage(ChatColor.RED + "Only pirates can have a parrot pet.");
			return false;
		}
		
		List<String> colors = new ArrayList<String>();
		colors.add("gray"); colors.add("green"); colors.add("aqua"); colors.add("blue"); colors.add("red");
		
		if (args.length == 0)
		{
			if (Main.plugin.parrots.containsValue(player.getUniqueId()))
			{
				removeOldParrot(player, (Parrot) player.getShoulderEntityLeft());
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
				return true;
			}
			else if (player.getShoulderEntityLeft() != null)
			{
				player.sendMessage(ChatColor.GRAY + "Don't try to remove that pretty parrot D:");
				return false;
			}
			else 
			{
				player.sendMessage(ChatColor.RED + "/parrot [gray/green/aqua/blue/red]");
				player.sendMessage(ChatColor.RED + "/parrot silent " + ChatColor.GRAY + "to silence your parrot.");
				player.sendMessage(ChatColor.RED + "/parrot " + ChatColor.GRAY + "to hide your parrot.");
				return false;
			}
		}
		
		if ((!colors.contains(args[0])) && (!args[0].equalsIgnoreCase("silent"))) {
			player.sendMessage(ChatColor.RED + "/parrot [gray/green/aqua/blue/red]");
			player.sendMessage(ChatColor.RED + "/parrot silent " + ChatColor.GRAY + "to silence your parrot.");
			player.sendMessage(ChatColor.RED + "/parrot " + ChatColor.GRAY + "to hide your parrot.");
			return false;
		}
		if (player.getShoulderEntityLeft() != null && (!Main.plugin.parrots.containsValue(player.getUniqueId())))
		{
			player.sendMessage(ChatColor.GRAY + "You have a regular parrot on your shoulder atm!");
			return false;
		}
		
		
		List<String> pets = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + player.getUniqueId().toString() + ".Pets");
		switch (args[0]) { 
			case "gray":
				if (pets.contains("gray_parrot")) {
					makeParrot(Variant.GRAY, player, false);
					return true;
				}
				else {
					player.sendMessage(ChatColor.GRAY + "You need to buy a gray parrot first.");
					return false;
				}
			case "green":
				if (pets.contains("green_parrot")) {
					makeParrot(Variant.GREEN, player, false);
					return true;
				}
				else {
					player.sendMessage(ChatColor.GRAY + "You need to buy a green parrot first.");
					return false;
				}
			case "blue":
				if (pets.contains("blue_parrot")) {
					makeParrot(Variant.BLUE, player, false);
					return true;
				}
				else {
					player.sendMessage(ChatColor.GRAY + "You need to buy a blue parrot first.");
					return false;
				}
			case "red":
				if (player.hasPermission("ftc.donator2")) {
					makeParrot(Variant.RED, player, false);
					return true;
				}
				else {
					player.sendMessage(ChatColor.GRAY + "Only captains can have a red parrot pet.");
					return false;
				}
			case "aqua":
				if (player.hasPermission("ftc.donator3")) {
					makeParrot(Variant.CYAN, player, false);
					return true;
				}
				else {
					player.sendMessage(ChatColor.GRAY + "Only admirals can have an aqua parrot pet.");
					return false;
				}
			case "silent":
				Parrot parrot = (Parrot) player.getShoulderEntityLeft();
				if (parrot != null) 
				{
					if (!parrot.isSilent())
					{
						player.sendMessage(ChatColor.GRAY + "Your parrot will stay quiet now.");
						Variant color = parrot.getVariant();
						removeOldParrot(player, parrot);
						makeParrot(color, player, true);
					}
					else
					{
						player.sendMessage(ChatColor.GRAY + "Your parrot will no longer stay quiet.");
						Variant color = parrot.getVariant();
						removeOldParrot(player, parrot);
						makeParrot(color, player, false);
					}
					return true;
				}
		}

		return false;
	}
	
	@SuppressWarnings("deprecation")
	private void removeOldParrot(Player player, Parrot parrot) {
		player.setShoulderEntityLeft(null);
		if (parrot != null) 
		{
			Main.plugin.parrots.remove(parrot.getUniqueId());
			parrot.remove();
		}
	}

	@SuppressWarnings("deprecation")
	private void makeParrot(Variant color, Player player, Boolean silent) {
		Parrot parrot = player.getWorld().spawn(player.getLocation(), Parrot.class);
		parrot.setVariant(color);
		parrot.setSilent(silent);
		Main.plugin.parrots.put(parrot.getUniqueId(), player.getUniqueId());
		player.setShoulderEntityLeft(parrot);
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
	}
	
}
