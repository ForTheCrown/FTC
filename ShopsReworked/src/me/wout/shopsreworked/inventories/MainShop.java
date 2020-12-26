package me.wout.shopsreworked.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainShop {

	public MainShop() {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 27, "FTC Shop");
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack itemShop = new ItemStack(Material.GOLD_BLOCK, 1);
		meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "-Item Shop-");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Sell vanilla items.");
		meta.setLore(lore);
		itemShop.setItemMeta(meta);
		
		ItemStack serverShop = new ItemStack(Material.EMERALD_BLOCK, 1);
		meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "-Server Shop-");
		lore.clear();
		lore.add(ChatColor.GRAY + "Online server shop.");
		meta.setLore(lore);
		serverShop.setItemMeta(meta);
		
		for (int i = 0; i < 10; i++) {
			inv.setItem(i, pane);
		}
		for (int i = 17; i < 27; i++) {
			inv.setItem(i, pane);
		}
		inv.setItem(11, itemShop);
		inv.setItem(15, serverShop);
		
		return inv;
	}
	
}
