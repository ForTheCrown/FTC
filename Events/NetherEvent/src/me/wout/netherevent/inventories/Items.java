package me.wout.netherevent.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import me.wout.netherevent.main;

public class Items implements Listener {

	main plugin;
	
	public Items(main plugin) {
		this.plugin = plugin;
	}
	
	private static ItemStack getPane() 
	{
		ItemStack result = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "");
		result.setItemMeta(meta);
		return result;
	}
	
	private static ItemStack getPreviousPage()
	{
		ItemStack result = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "< Previous Page");
		result.setItemMeta(meta);
		return result;
	}
	
	private static ItemStack getNextPage()
	{
		ItemStack result = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Next Page >");
		result.setItemMeta(meta);
		return result;
	}
	
	private static void makeItemPretty(ItemStack item) 
	{
		ItemMeta meta = item.getItemMeta();
		String rname = "";
		String name = item.getType().toString().toLowerCase();
		String[] name2 = name.split("_");
		for (int i = 0; i < name2.length; i++) {
			name2[i] = name2[i].replaceFirst(name2[i].substring(0, 1), name2[i].substring(0, 1).toUpperCase()) + " ";
			rname = rname + name2[i];
		}
		meta.setDisplayName(ChatColor.AQUA + rname);
		List<String> lore = new ArrayList<String>();
		lore.add(net.md_5.bungee.api.ChatColor.of("#808080") + "Click to check for this item.");
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	public Inventory getFirstInv(String uuid) {
		Inventory inv = Bukkit.createInventory(null, 54, "1 Point Items");
		
		ItemStack pane = getPane();
		ItemStack nextpage = getNextPage();
		
		for (int i = 0; i < 9; i++)
			inv.setItem(i, pane);
		for (int i = 45; i < 54; i++)
			inv.setItem(i, pane);
		for (int i = 9; i < 54; i = i + 9)
			inv.setItem(i, pane);
		for (int i = 17; i < 54; i = i + 9)
			inv.setItem(i, pane);
		
		inv.setItem(8, nextpage);
		
		Material[] pointlist = {Material.NETHERRACK, Material.NETHER_BRICKS, Material.GRAVEL, Material.MAGMA_BLOCK, Material.POLISHED_BASALT,
				Material.ROTTEN_FLESH, Material.BONE, Material.QUARTZ, Material.TWISTING_VINES, Material.SHROOMLIGHT,
				Material.SOUL_CAMPFIRE, Material.WARPED_STEM, Material.WARPED_FUNGUS, Material.CRIMSON_FUNGUS, Material.CRIMSON_STEM,
				Material.BLAZE_ROD, Material.GOLD_NUGGET, Material.COOKED_PORKCHOP, Material.GLOWSTONE, Material.NETHER_BRICK};
		int pointer = 0;
		
		for (int row = 0; row < 4; row++)
		{
			for (int slot = 0; slot < 5; slot++) 
			{
				ItemStack item = new ItemStack(pointlist[pointer++], 1);
				makeItemPretty(item);
				inv.setItem(11 + (9*row) + slot, item);
			}
		}
		return enchantAlreadyGotten(inv, uuid, 1);
	}


	public Inventory getSecondInv(String uuid) {
		Inventory inv = Bukkit.createInventory(null, 54, "10 Point Items");
		
		ItemStack pane = getPane();
		ItemStack nextpage = getNextPage();
		ItemStack previouspage = getPreviousPage();
		
		for (int i = 0; i < 9; i++)
			inv.setItem(i, pane);
		for (int i = 45; i < 54; i++)
			inv.setItem(i, pane);
		for (int i = 9; i < 54; i = i + 9)
			inv.setItem(i, pane);
		for (int i = 17; i < 54; i = i + 9)
			inv.setItem(i, pane);
		
		inv.setItem(0, previouspage);
		inv.setItem(8, nextpage);
		
		Material[] pointlist = {Material.FIREWORK_STAR, Material.GOLDEN_HOE, Material.GOLD_BLOCK, Material.GOLDEN_HORSE_ARMOR, Material.BREWING_STAND,
				Material.TORCH, Material.IRON_INGOT, Material.DIAMOND_HORSE_ARMOR, Material.PAINTING, Material.COAL,
				Material.SOUL_LANTERN, Material.LANTERN, Material.CRYING_OBSIDIAN, Material.MAGMA_CREAM, Material.ENDER_PEARL,
				Material.CROSSBOW, Material.WARPED_FUNGUS_ON_A_STICK, Material.FLINT_AND_STEEL, Material.RED_NETHER_BRICKS, Material.SADDLE};
		int pointer = 0;
		for (int row = 0; row < 4; row++)
		{
			for (int slot = 0; slot < 5; slot++) 
			{
				ItemStack item = new ItemStack(pointlist[pointer++], 1);
				makeItemPretty(item);
				inv.setItem(11 + (9*row) + slot, item);
			}
		}
		return enchantAlreadyGotten(inv, uuid, 2);
	}
	
	public Inventory getThirdInv(String uuid) {
		Inventory inv = Bukkit.createInventory(null, 54, "50-100 Point Items");
		
		ItemStack pane = getPane();
		ItemStack previouspage = getPreviousPage();
		
		for (int i = 0; i < 9; i++)
			inv.setItem(i, pane);
		for (int i = 45; i < 54; i++)
			inv.setItem(i, pane);
		for (int i = 9; i < 54; i = i + 9)
			inv.setItem(i, pane);
		for (int i = 17; i < 54; i = i + 9)
			inv.setItem(i, pane);
		
		inv.setItem(0, previouspage);
		
		Material[] pointlist1 = {Material.EGG, Material.DIAMOND, Material.WITHER_ROSE, Material.FEATHER, Material.LAVA_BUCKET, Material.GILDED_BLACKSTONE, Material.ENDER_CHEST, Material.RESPAWN_ANCHOR};
		Material[] pointlist2 = {Material.WITHER_SKELETON_SKULL, Material.NETHERITE_SCRAP, Material.NETHERITE_INGOT};
		int[] slots1 = {11, 12, 13, 14, 15, 21, 22, 23};
		int[] slots2 = {39, 40, 41};
		
		int pointer = 0;
		for (int j = 0; j < slots1.length; j++) 
		{
			ItemStack item = new ItemStack(pointlist1[pointer++], 1);
			makeItemPretty(item);
			inv.setItem(slots1[j], item);
		}
		pointer = 0;
		for (int j = 0; j < slots2.length; j++) 
		{
			ItemStack item = new ItemStack(pointlist2[pointer++], 1);
			makeItemPretty(item);
			inv.setItem(slots2[j], item);
		}
		
		return enchantAlreadyGotten(inv, uuid, 3);
	}

	private Inventory enchantAlreadyGotten(Inventory inv, String uuid, int type) {
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(plugin.getFile(uuid));
		List<String> list = new ArrayList<String>();
		String[] sections = {"", ""};
		switch (type) {
		case 1: sections[0] = "FirstInv"; break;
		case 2: sections[0] = "SecondInv"; break;
		case 3: sections[0] = "ThirdInv";  sections[1] = "Extra"; break;
		default: sections[0] = "FirstInv"; break;
		}
		
		for (String section : sections)
		{
			if (section == "") continue;
			
			list = yaml.getStringList("Items_Found." + section);
			if (!list.isEmpty()) 
			{
				for (String found_item : list)
				{
					for (ItemStack invItem : inv.getContents())
					{
						if (invItem != null && invItem.getType() == Material.getMaterial(found_item)) 
						{
							invItem.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
							ItemMeta meta = invItem.getItemMeta();
							meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							
							invItem.setItemMeta(meta);
						}
					}
				}
			}
		}
		return inv;
	}
	
	
	//--------------------------------------------------------------------------------------//
	
	
	@EventHandler
	public void onPlayerClickItemInInv(InventoryClickEvent event) {
		String title = event.getView().getTitle();
		int slot = event.getSlot();
		
		if (title.contains("1 Point Items")) {
			event.setCancelled(true);
			if (event.getClickedInventory() instanceof PlayerInventory) return;

			Player player = (Player) event.getWhoClicked();
			
			if (slot == 8) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(getSecondInv(player.getUniqueId().toString()));
				return;
			}
			else if ((slot > 9 && slot < 17) || (slot > 18 && slot < 26) || (slot > 27 && slot < 35) || (slot > 36 && slot < 44)) 
			{
				if (event.getInventory().getItem(slot) == null) return;
				else 
				{
					tryGivePoints(player, event.getInventory().getItem(slot), 1);
					return;
				}
			}
		}
		
		else if (title.contains("10 Point Items")) {
			event.setCancelled(true);
			if (event.getClickedInventory() instanceof PlayerInventory) return;
			
			Player player = (Player) event.getWhoClicked();
			
			if (slot == 0) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(getFirstInv(player.getUniqueId().toString()));
				return;
			}
			else if (slot == 8) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(getThirdInv(player.getUniqueId().toString()));
				return;
			}
			else if ((slot > 9 && slot < 17) || (slot > 18 && slot < 26) || (slot > 27 && slot < 35) || (slot > 36 && slot < 44)) 
			{
				if (event.getInventory().getItem(slot) == null) return;
				else 
				{
					tryGivePoints(player, event.getInventory().getItem(slot), 2);
					return;
				}
			}
		}
		
		else if (title.contains("50-100 Point Items")) {
			event.setCancelled(true);
			if (event.getClickedInventory() instanceof PlayerInventory) return;
			
			Player player = (Player) event.getWhoClicked();
			
			if (slot == 0) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(getSecondInv(player.getUniqueId().toString()));
				return;
			}
			else if ((slot > 9 && slot < 17) || (slot > 18 && slot < 26)) 
			{
				if (event.getInventory().getItem(slot) == null) return;
				else 
				{
					tryGivePoints(player, event.getInventory().getItem(slot), 3);
					return;
				}
			}
			else if (slot > 36 && slot < 44) 
			{
				if (event.getInventory().getItem(slot) == null) return;
				else 
				{
					tryGivePoints(player, event.getInventory().getItem(slot), 4);
					return;
				}
			}
		}
	}
	
	private void tryGivePoints(Player player, ItemStack item, int type) {
		if (!player.getWorld().getName().contains("nether_event"))
		{
			player.sendMessage(ChatColor.GRAY + "This can only be done in the netherevent world.");
			return;
		}
		Material materialToCheck = item.getType();
		String section;
		switch (type) {
		case (1): section = "Items_Found.FirstInv"; break;
		case (2): section = "Items_Found.SecondInv"; break;
		case (3): section = "Items_Found.ThirdInv"; break;
		case (4): section = "Items_Found.Extra"; break;
		default: section = "Items_Found.FirstInv"; break;
		}
		
		for (ItemStack invItem : player.getInventory().getContents())
		{
			if (invItem != null && materialToCheck == invItem.getType())
			{
				YamlConfiguration yaml = YamlConfiguration.loadConfiguration(plugin.getFile(player.getUniqueId().toString()));
				List<String> gottenItems = yaml.getStringList(section);
				if (gottenItems.contains(materialToCheck.toString()))
				{
					player.sendMessage(ChatColor.GRAY + "You've already gotten points from this item.");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 0.5f);
					return;
				}
				else 
				{
					gottenItems.add(materialToCheck.toString());
					yaml.set(section, gottenItems);
					plugin.saveyaml(yaml, plugin.getFile(player.getUniqueId().toString()));
					addScore(section, player);
					player.closeInventory();
					return;
				}
			}
		}
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 0.5f);
		return;
	}

	@SuppressWarnings("deprecation")
	private void addScore(String section, Player player) {
		Objective pp = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("crown");
		Score ppp = pp.getScore(player);
		
		int toadd = 1;
		if (section.contains("SecondInv")) toadd = 10;
		else if (section.contains("ThirdInv")) toadd = 50;
		else if (section.contains("Extra")) toadd = 100;
		
		ppp.setScore(ppp.getScore() + toadd);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
		if (ppp.getScore() == 1) player.sendMessage(ChatColor.GRAY + "[FTC] You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " point.");
		else player.sendMessage(ChatColor.GRAY + "[FTC] You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " points.");
	}
}
