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

public class Regions {

	public Regions(main plugin) {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 27, "Regions");
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack book = new ItemStack(Material.BOOK, 1);
		meta = book.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Regions");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Naming a 400x400 region.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		book.setItemMeta(meta);
		
		ItemStack freet = new ItemStack(Material.PAPER, 1);
		meta = freet.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Region Name Ticket");
		lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE + "Trade this with an online");
		lore.add(ChatColor.LIGHT_PURPLE + "staff member to allow " + ChatColor.BOLD + "them" + ChatColor.LIGHT_PURPLE + " to");
		lore.add(ChatColor.LIGHT_PURPLE + "name your region.");
		lore.add(ChatColor.GRAY + "Costs 50,000 Rhines. Regions can only be named");
		lore.add(ChatColor.GRAY + "if they are as developed as Hazelguard and if");
		lore.add(ChatColor.GRAY + "the owner of the region has any rank.");
		meta.setLore(lore);
		freet.setItemMeta(meta);
		
		ItemStack paidt = new ItemStack(Material.PAPER, 1);
		meta = paidt.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Region Name Ticket+");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Costs €10.00 in the webstore.");
		lore.add(ChatColor.GRAY + "You can choose a name for your region.");
		meta.setLore(lore);
		paidt.setItemMeta(meta);
		
		
		ItemStack prevpage = new ItemStack(Material.PAPER, 1);
		meta = prevpage.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "< Previous Page");
		prevpage.setItemMeta(meta);
		
		for (int i = 0; i < 10; i++) {
			inv.setItem(i, pane);
		}
		for (int i = 17; i < 27; i++) {
			inv.setItem(i, pane);
		}
		inv.setItem(0, prevpage);
		inv.setItem(4, book);
		inv.setItem(12, freet);
		inv.setItem(14, paidt);
		
		
		return inv;
	}
	
	
}
