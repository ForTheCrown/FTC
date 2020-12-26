package me.wout.shopsreworked.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.wout.shopsreworked.main;

public class SellShop {

	private main plugin;
	
	public Inventory mainItemShopInventory;
	public int invType = 0;
	public String amount = "all";
	private String playerUUID = "";
	
	public SellShop(main plugin, Inventory inv, int invtype, String amount, String playerUUID) {
		this.playerUUID = playerUUID;
		this.amount = amount;
		this.invType = invtype;
		this.mainItemShopInventory = inv;
		this.plugin = plugin;
	}
	
	public Inventory getInv() {
		Inventory inv = Bukkit.createInventory(null, 54, "Selling Items");
		for (int i = 0; i < 54; i++) {
			if (mainItemShopInventory.getItem(i) != null)
				inv.setItem(i, mainItemShopInventory.getItem(i));
		}
		
		ItemStack label = new ItemStack(Material.STONE);
		ItemMeta meta;
		List<String> lore = new ArrayList<String>();
		
		if (invType == 0) {
			label = new ItemStack(Material.OAK_SAPLING, 1);
			meta = label.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Farming");
			lore.add(ChatColor.GRAY + "Crops and other farmable items.");
			meta.setLore(lore);
			label.setItemMeta(meta);
			setStock(inv);
		}
		else if (invType == 1) {
			label = new ItemStack(Material.IRON_PICKAXE, 1);
			meta = label.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Mining");
			lore.add(ChatColor.GRAY + "Ores and common blocks.");
			meta.setLore(lore);
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			label.setItemMeta(meta);
			setStock(inv);
		}
		else if (invType == 2) {
			label = new ItemStack(Material.ROTTEN_FLESH, 1);
			meta = label.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Drops");
			lore.add(ChatColor.GRAY + "Common mobdrops.");
			meta.setLore(lore);
			label.setItemMeta(meta);
			setStock(inv);
		}
		inv.setItem(4, label);
		
		ItemStack amountpane1 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		meta = amountpane1.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Sell 1");
		lore.add(ChatColor.GRAY + "Set the amount of items you");
		lore.add(ChatColor.GRAY + "will sell per click.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		amountpane1.setItemMeta(meta);
		
		ItemStack amountpane16 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		meta = amountpane16.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Sell per 16");
		lore.clear();
		lore.add(ChatColor.GRAY + "Set the amount of items you");
		lore.add(ChatColor.GRAY + "will sell per click.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		amountpane16.setItemMeta(meta);
		
		ItemStack amountpane64 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		meta = amountpane64.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Sell per 64");
		lore.clear();
		lore.add(ChatColor.GRAY + "Set the amount of items you");
		lore.add(ChatColor.GRAY + "will sell per click.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		amountpane64.setItemMeta(meta);
		
		ItemStack amountpaneall = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		meta = amountpaneall.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + "Sell All");
		lore.clear();
		lore.add(ChatColor.GRAY + "Lets you sell all items of a");
		lore.add(ChatColor.GRAY + "specific type at once.");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		amountpaneall.setItemMeta(meta);
		
		if (amount == "all") {
			meta = amountpaneall.getItemMeta();
			meta.addEnchant(Enchantment.CHANNELING, 1, false);
			amountpaneall.setItemMeta(meta);
		}
		else if (amount == "64") {
			meta = amountpane64.getItemMeta();
			meta.addEnchant(Enchantment.CHANNELING, 1, false);
			amountpane64.setItemMeta(meta);
		}
		else if (amount == "1") {
			meta = amountpane1.getItemMeta();
			meta.addEnchant(Enchantment.CHANNELING, 1, false);
			amountpane1.setItemMeta(meta);
		}
		else if (amount == "16") {
			meta = amountpane16.getItemMeta();
			meta.addEnchant(Enchantment.CHANNELING, 1, false);
			amountpane16.setItemMeta(meta);
		}
		
		inv.setItem(17, amountpane1);
		inv.setItem(26, amountpane16);
		inv.setItem(35, amountpane64);
		inv.setItem(44, amountpaneall);
		
		return inv;
	}
	
	private void setStock(Inventory inv) {
		if (invType == 0) {
			List<ItemStack> stock = new ArrayList<ItemStack>();
			setItems("Crops", stock);
			
			for (int i = 0; i < 5; i++) {
				inv.setItem(i+20, stock.get(i));
			}
			for (int i = 0; i < 5; i++) {
				inv.setItem(i+29, stock.get(i+5));
			}
			for (int i = 0; i < 5; i++) {
				inv.setItem(i+38, stock.get(i+10));
			}
		}
		else if (invType == 1) {
			List<ItemStack> stock = new ArrayList<ItemStack>();
			setItems("Mining", stock);
			for (int j = 0; j < 4; j++) {
				int k = j;
				if (k > 1) k++;
				for (int i = 0; i < 4; i++) {
					inv.setItem(11+(i*9)+k, stock.get(i+(j*4)));
				}
			}
			
		}
		else if (invType == 2) {
			List<ItemStack> stock = new ArrayList<ItemStack>();
			setItems("MobDrops", stock);
			for (int i = 0; i < 5; i++) {
				inv.setItem(i+20, stock.get(i));
			}
			for (int i = 0; i < 5; i++) {
				inv.setItem(i+29, stock.get(i+5));
			}
		}
		else {
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Shops] Wrong invType for shop inventory happened!");
		}
	}
	
	private void setItems(String configSection, List<ItemStack> items) {
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add("");
		lore.add(ChatColor.GRAY + "Amount of items you will sell: " + amount + ".");
		lore.add(ChatColor.GRAY + "Change the amount setting on the right.");
		
		plugin.loadFiles();
		
		for (String mat : plugin.getConfig().getConfigurationSection("Price_Per_Item." + configSection).getKeys(false)) {
			ItemStack item = new ItemStack(Material.getMaterial(mat), 1);
			ItemMeta meta = item.getItemMeta();
			
			String rname = "";
			String name = item.getType().toString().toLowerCase();
			String[] name2 = name.split("_");
			for (int i = 0; i < name2.length; i++) {
				name2[i] = name2[i].replaceFirst(name2[i].substring(0, 1), name2[i].substring(0, 1).toUpperCase()) + " ";
				rname = rname + name2[i];
			}
			meta.setDisplayName(ChatColor.AQUA + rname);
			
			
			int price = getCorrectPriceDisplay(configSection + "." + mat); //new
			
			lore.set(0, ChatColor.YELLOW + "Value: " + price + " Rhines per item.");
			lore.set(1, ChatColor.GOLD + "" + price*64 + " Rhines for a stack");
			meta.setLore(lore);
			item.setItemMeta(meta);
			items.add(item);
		}
		
		plugin.unloadFiles();
	}
	
	private int getCorrectPriceDisplay(String path) {
		
		YamlConfiguration playerFileYaml = YamlConfiguration.loadConfiguration(plugin.getFile(this.playerUUID));
		int result = plugin.getPrice(playerFileYaml, path);
		
		return result;
	}

	
}
