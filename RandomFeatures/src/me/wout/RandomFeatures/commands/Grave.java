package me.wout.RandomFeatures.commands;

import org.bukkit.command.CommandExecutor;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.wout.RandomFeatures.Main;

public class Grave implements CommandExecutor{
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Checks if sender is a player.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can do this.");
		}
		Player player = (Player) sender;
		
		if (inList(player.getUniqueId().toString()) == true) {
			openGrave(player);
			return true;
		}
		
		player.sendMessage(ChatColor.GRAY + "[FTC] Your grave does not contain any items.");
		return false;
	}

	private void openGrave(Player player) {
		@SuppressWarnings("unchecked")
		List<ItemStack> itemsFromGrave = (List<ItemStack>) Main.plugin.gravesyaml.getList(player.getUniqueId().toString());
		
		// Count grave items.
		int counter = 0;
		for (ItemStack item : player.getInventory().getStorageContents()) {
			if (item == null) {
				counter++;
			}
		}
		
		// Check if enough empty slots in inventory.
		if (counter >= itemsFromGrave.size()) {
			for (ItemStack item : itemsFromGrave) {
				item.getItemMeta().setDisplayName(ChatColor.RESET + item.getItemMeta().getDisplayName());
				player.getInventory().addItem(item);
			}
			Main.plugin.gravesyaml.set(player.getUniqueId().toString(), null);
			player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 1.2f, 1f);
			player.sendMessage(ChatColor.GRAY + "You retrieved all the items from your grave.");
		}
		else {
			player.sendMessage(ChatColor.RED + "[FTC] " + ChatColor.GRAY + "You don't have enough space in your inventory.");
		}
			
		
		Main.plugin.unloadFiles();
	}

	private boolean inList(String uniqueId) {
		Main.plugin.loadFiles();
		if (Main.plugin.gravesyaml.getList(uniqueId) == null) {
			Main.plugin.unloadFiles();
			return false;
		}
		else {
			return true;
		}
		
	}
}
