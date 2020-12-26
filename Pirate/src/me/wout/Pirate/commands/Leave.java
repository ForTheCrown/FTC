package me.wout.Pirate.commands;

import org.bukkit.command.CommandExecutor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.wout.Pirate.Main;

public class Leave implements CommandExecutor{
	
	public Leave() {
		Main.plugin.getCommand("leave").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		
		Player player = (Player) sender;
		Location playerloc = player.getLocation();
		if (!(playerloc.getWorld().getName().contains("world_void") && (playerloc.getX() < -880) && (playerloc.getZ() < 96) && (playerloc.getZ() > -520)))
		{
			player.sendMessage(ChatColor.GRAY + "This command can only be executed in the grappling parkour.");
			return false;
		}
		
		player.getInventory().clear();
		player.teleport(new Location(Bukkit.getWorld("world_void"), -800.5, 232, 11.5, -90, 0));
		player.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), 200, 70, 1000), true);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		return true;
	}
	
	
}
