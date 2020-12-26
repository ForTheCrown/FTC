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

public class Heads {

	public Heads(main plugin) {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 27, "Heads");
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack hat = new ItemStack(Material.PLAYER_HEAD, 1);
		meta = hat.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Heads");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Decorative player heads.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		hat.setItemMeta(meta);
		
		ItemStack mobs = new ItemStack(Material.SKELETON_SKULL, 1);
		meta = mobs.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Mob skull package.");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Costs €3.00 in the webstore.");
		lore.add(ChatColor.GRAY + "Zombie head, Skeleton skull, Creeper head,");
		lore.add(ChatColor.GRAY + "Wither Skeleton skull and a Dragon Head.");
		meta.setLore(lore);
		mobs.setItemMeta(meta);
		
		ItemStack playerh = new ItemStack(Material.SKELETON_SKULL, 1);
		meta = playerh.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Playerhead");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Costs €3.00 in the webstore.");
		lore.add(ChatColor.GRAY + "You can ask us to trade this to any playerhead you want.");
		meta.setLore(lore);
		playerh.setItemMeta(meta);
		
		
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
		inv.setItem(4, hat);
		inv.setItem(12, mobs);
		inv.setItem(14, playerh);
		
		
		return inv;
	}
	
	
}
