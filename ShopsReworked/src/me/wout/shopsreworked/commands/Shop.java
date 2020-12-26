package me.wout.shopsreworked.commands;

import org.bukkit.command.CommandExecutor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.wout.shopsreworked.main;
import me.wout.shopsreworked.inventories.MainShop;

public class Shop implements CommandExecutor{
	
	private MainShop ms;
	
	public Shop(main plugin) {
		this.ms = new MainShop();
		plugin.getCommand("shop").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
			return false;
		}
		
		Player player = (Player) sender;
		player.openInventory(getInv(player));
		
		return true;
	}
	
	public Inventory getInv(Player player) {
		if (ms.getInv() == null) {
			player.sendMessage(ChatColor.RED + "Something went wrong. Contact Wout.");
			return null;
		}
		return ms.getInv();
	}
}
