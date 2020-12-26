package me.wout.netherevent.commands;

import org.bukkit.command.CommandExecutor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.wout.netherevent.main;
import me.wout.netherevent.inventories.Items;

public class ItemList implements CommandExecutor{
	
	private Items fi;
	
	public ItemList(main plugin) {
		this.fi = new Items(plugin);
		plugin.getServer().getPluginManager().registerEvents(fi, plugin);
		plugin.getCommand("itemlist").setExecutor(this);
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
		player.openInventory(fi.getFirstInv(player.getUniqueId().toString()));
		return true;
	}
}
