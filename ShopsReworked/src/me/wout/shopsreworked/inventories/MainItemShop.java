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

public class MainItemShop {

	public MainItemShop(main plugin) {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 27, "Item Shop");
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack crops = new ItemStack(Material.OAK_SAPLING, 1);
		meta = crops.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Farming");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Crops and other farmable items.");
		meta.setLore(lore);
		crops.setItemMeta(meta);
		
		ItemStack mining = new ItemStack(Material.IRON_PICKAXE, 1);
		meta = mining.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Mining");
		lore.clear();
		lore.add(ChatColor.GRAY + "Ores and common blocks.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		mining.setItemMeta(meta);
		
		ItemStack mobdrops = new ItemStack(Material.ROTTEN_FLESH, 1);
		meta = mobdrops.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Drops");
		lore.clear();
		lore.add(ChatColor.GRAY + "Common mobdrops.");
		meta.setLore(lore);
		mobdrops.setItemMeta(meta);
		
		for (int i = 0; i < 10; i++) {
			inv.setItem(i, pane);
		}
		for (int i = 17; i < 27; i++) {
			inv.setItem(i, pane);
		}
		inv.setItem(11, crops);
		inv.setItem(13, mining);
		inv.setItem(15, mobdrops);
		
		return inv;
	}
	
	
}
