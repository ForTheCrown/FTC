package me.wout.halloween.commands;

import org.bukkit.command.CommandExecutor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.wout.halloween.main;

public class Leave implements CommandExecutor{

	main plugin;
	
	public Leave(main plugin) {
		this.plugin = plugin;
		plugin.getCommand("leave").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		
		Player player = (Player) sender;
		if (!player.getWorld().getName().contains("nether_event"))
		{
			player.sendMessage(ChatColor.GRAY + "This command can only be executed in the netherevent world.");
			return false;
		}
		
		if (!plugin.invClear(player))
		{
			player.sendMessage(ChatColor.GRAY + "Your inventory has to be empty to leave.");
			return false;
		}
		
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(plugin.getFile(player.getUniqueId().toString()));
		yaml.set("Leave_Location.x", player.getLocation().getBlockX());
		yaml.set("Leave_Location.y", player.getLocation().getBlockY());
		yaml.set("Leave_Location.z", player.getLocation().getBlockZ());
		plugin.saveyaml(yaml, plugin.getFile(player.getUniqueId().toString()));
		
		
		player.teleport(new Location(Bukkit.getWorld(plugin.getConfig().getString("ReturnLocation.World")), plugin.getConfig().getDouble("ReturnLocation.x"), plugin.getConfig().getDouble("ReturnLocation.y"), plugin.getConfig().getDouble("ReturnLocation.z"), -90, 0));
		player.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), 200, 70, 1000), true);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		player.getInventory().clear();
		return true;
	}
	
	
}
