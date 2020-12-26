package me.wout.shopsreworked.inventories;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.wout.shopsreworked.main;

public class Trade {

	public Trade(main plugin) {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 54);
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack prevpage = new ItemStack(Material.PAPER, 1);
		meta = prevpage.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "< Previous Page");
		prevpage.setItemMeta(meta);
		
		for (int i = 0; i < 9; i++)
			inv.setItem(i, pane);
		for (int i = 45; i < 54; i++)
			inv.setItem(i, pane);
		for (int i = 9; i < 54; i = i + 9)
			inv.setItem(i, pane);
		for (int i = 17; i < 54; i = i + 9)
			inv.setItem(i, pane);
		
		inv.setItem(0, prevpage);
		
		return inv;
	}
	
	
}
