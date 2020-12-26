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

public class Ranks {

	public Ranks(main plugin) {
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 27, "Ranks");
		
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		ItemStack hat = new ItemStack(Material.GOLDEN_HELMET, 1);
		meta = hat.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Ranks");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "All the ranks available on FTC.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		hat.setItemMeta(meta);
		
		ItemStack rankKnight = new ItemStack(Material.NAME_TAG, 1);
		meta = rankKnight.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Knight" + ChatColor.DARK_GRAY + "]");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Acquired by completing the first three levels in the");
		lore.add(ChatColor.GRAY + "dungeons and giving the apples to Diego in the shop.");
		meta.setLore(lore);
		rankKnight.setItemMeta(meta);
		
		ItemStack rankBaron = new ItemStack(Material.NAME_TAG, 1);
		meta = rankBaron.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Baron" + ChatColor.DARK_GRAY + "] / [" + ChatColor.GRAY + "Baroness" + ChatColor.DARK_GRAY + "]");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Acquired by paying 500,000 Rhines.");
		meta.setLore(lore);
		rankBaron.setItemMeta(meta);
		
		ItemStack rankLord = new ItemStack(Material.NAME_TAG, 1);
		meta = rankLord.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.of("#959595") + "[" + ChatColor.GOLD + "Lord" + net.md_5.bungee.api.ChatColor.of("#959595") + "] / [" + ChatColor.GOLD + "Lady" + net.md_5.bungee.api.ChatColor.of("#959595") + "]");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Costs €10.00 in the webstore.");
		meta.setLore(lore);
		rankLord.setItemMeta(meta);
		
		ItemStack rankDuke = new ItemStack(Material.NAME_TAG, 1);
		meta = rankDuke.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "[" + net.md_5.bungee.api.ChatColor.of("#ffcd00") + "Duke" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "] / [" + net.md_5.bungee.api.ChatColor.of("#ffcd00") + "Duchess" + net.md_5.bungee.api.ChatColor.of("#bfbfbf") + "]");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Costs €20.00 in the webstore.");
		meta.setLore(lore);
		rankDuke.setItemMeta(meta);
		
		ItemStack rankPrince = new ItemStack(Material.NAME_TAG, 1);
		meta = rankPrince.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "[" + net.md_5.bungee.api.ChatColor.of("#fbff0f") + "Prince" + ChatColor.WHITE + "] / [" +net.md_5.bungee.api.ChatColor.of("#fbff0f") + "Princess" + ChatColor.WHITE + "]");
		lore = new ArrayList<String>(); 
		lore.add(ChatColor.GRAY + "Costs €6.00 per month in the webstore.");
		lore.add(ChatColor.GRAY + "Requires the Duke rank.");
		meta.setLore(lore);
		rankPrince.setItemMeta(meta);
		
		ItemStack rankLegend = new ItemStack(Material.NAME_TAG, 1);
		meta = rankLegend.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.of("#dfdfdf") + "[" + net.md_5.bungee.api.ChatColor.of("#fff147") + "Legend" + net.md_5.bungee.api.ChatColor.of("#dfdfdf") + "]");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Costs €50.00 in the webstore.");
		meta.setLore(lore);
		rankLegend.setItemMeta(meta);
		
		ItemStack rankKing = new ItemStack(Material.NAME_TAG, 1);
		meta = rankKing.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "[" + ChatColor.YELLOW + ChatColor.BOLD + "King" + ChatColor.RESET + "] / [" + ChatColor.YELLOW + ChatColor.BOLD + "Queen" + ChatColor.WHITE + "]");
		lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Acquired by winning the Crown.");
		lore.add(ChatColor.GRAY + "Rank is lost when someone else wins the Crown.");
		meta.setLore(lore);
		rankKing.setItemMeta(meta);
		
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
		inv.setItem(10, rankKnight);
		inv.setItem(11, rankBaron);
		inv.setItem(12, rankLord);
		inv.setItem(13, rankDuke);
		inv.setItem(15, rankPrince);
		inv.setItem(14, rankLegend);
		inv.setItem(16, rankKing);
		
		
		return inv;
	}
	
	
}
