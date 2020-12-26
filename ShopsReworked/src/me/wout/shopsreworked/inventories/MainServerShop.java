package me.wout.shopsreworked.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.wout.shopsreworked.main;

public class MainServerShop {

	public MainServerShop(main plugin) {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 27, "Server Shop");
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack ranks = new ItemStack(Material.NAME_TAG, 1);
		meta = ranks.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Ranks");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "All the ranks available on FTC.");
		meta.setLore(lore);
		ranks.setItemMeta(meta);
		
		ItemStack heads = new ItemStack(Material.SKELETON_SKULL, 1);
		meta = heads.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Heads");
		lore.clear();
		lore.add(ChatColor.GRAY + "Decorative player heads.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		heads.setItemMeta(meta);
		
		ItemStack regions = new ItemStack(Material.BOOK, 1);
		meta = regions.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Regions");
		lore.clear();
		lore.add(ChatColor.GRAY + "Naming a 400x400 region.");
		meta.setLore(lore);
		regions.setItemMeta(meta);
		
		for (int i = 0; i < 10; i++) {
			inv.setItem(i, pane);
		}
		for (int i = 17; i < 27; i++) {
			inv.setItem(i, pane);
		}
		inv.setItem(11, ranks);
		inv.setItem(13, heads);
		inv.setItem(15, regions);
		
		return inv;
	}
	
	
}
