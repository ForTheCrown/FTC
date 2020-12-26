package me.wout.shopsreworked;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.*;

import me.wout.shopsreworked.commands.Shop;
import me.wout.shopsreworked.inventories.Heads;
import me.wout.shopsreworked.inventories.MainItemShop;
import me.wout.shopsreworked.inventories.MainServerShop;
import me.wout.shopsreworked.inventories.Ranks;
import me.wout.shopsreworked.inventories.Regions;
import me.wout.shopsreworked.inventories.SellShop;
import me.wout.shopsreworked.inventories.Trade;
import me.wout.shopsreworked.signshopClicks.AdminBuy;
import me.wout.shopsreworked.signshopClicks.AdminSell;
import me.wout.shopsreworked.signshopClicks.GenericBuy;
import me.wout.shopsreworked.signshopClicks.GenericSell;
import me.wout.shopsreworked.signshopClicks.SignShop;

public class main extends JavaPlugin implements Listener {

	File shopfiles;
	public File moneyfile;
	public YamlConfiguration cashyaml;
	public File blackmarketFile;
	public YamlConfiguration blackmarketYaml;
	public File playerDataFile;
	Map<String,Inventory> invs = new HashMap<String,Inventory>();
	Map<String,SignChangeEvent> signs = new HashMap<String,SignChangeEvent>();
	public Map<Inventory, File> editInvs = new HashMap<Inventory, File>();
	Set<Material> allSigns = new HashSet<>(Arrays.asList(
		Material.OAK_SIGN, Material.OAK_WALL_SIGN, 
		Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN, 
		Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN, 
		Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN, 
		Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
		Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
		Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN,
		Material.WARPED_SIGN, Material.WARPED_WALL_SIGN)); 
	
	Map<String, String> amountSetting = new HashMap<String, String>();
	Map<String, Integer> invTypeSetting = new HashMap<String, Integer>();
	Shop shop;
	MainItemShop mainItemShop;
	SellShop sellShopFall;
	SellShop sellShopMall;
	SellShop sellShopDall;
	SellShop sellShopF64;
	SellShop sellShopM64;
	SellShop sellShopD64;
	SellShop sellShopF1;
	SellShop sellShopM1;
	SellShop sellShopD1;
	SellShop sellShopF16;
	SellShop sellShopM16;
	SellShop sellShopD16;
	Trade trade;
	MainServerShop mainServerShop;
	Ranks ranks;
	Heads heads;
	Regions regions;
	//RanksCommand rankscommand;
	
	Set<String> onCooldown = new HashSet<String>();
	
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		
		// Check datafolder.
		File dir = getDataFolder();
		if (!dir.exists())
			if (!dir.mkdir())
				System.out.println("Could not create directory for plugin: " + getDescription().getName());
		
		
		
		shop = new Shop(this);
		trade = new Trade(this);
		//rankscommand = new RanksCommand(this);
		loadFiles();
		unloadFiles();
	}
	
	public void onDisable() {
		loadFiles();
		saveyaml(cashyaml, moneyfile);
		unloadFiles();
	}
	
	public void loadFiles() {
		shopfiles = new File(getDataFolder(), File.separator + "ShopData");
		if(!shopfiles.exists()){
            shopfiles.mkdirs();
        }
		
		playerDataFile = new File(getDataFolder(), File.separator + "PlayerData");
		if(!playerDataFile.exists()){
			playerDataFile.mkdirs();
        }
		

		moneyfile = new File(getDataFolder(), "PlayerBalances.yml");
		if(!moneyfile.exists()){
			try {
				moneyfile.createNewFile();
				cashyaml = YamlConfiguration.loadConfiguration(moneyfile);
				cashyaml.createSection("Players");
				cashyaml.set("Players", new ArrayList<String>());
				cashyaml.createSection("PlayerData");
				saveyaml(cashyaml, moneyfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	cashyaml = YamlConfiguration.loadConfiguration(moneyfile);
        }
		
		blackmarketFile = new File(getDataFolder(), "BlackMarketPrices.yml");
		if (!blackmarketFile.exists()) {
			this.getServer().getConsoleSender().sendMessage("BlackMarketPrices.yml not found in datafolder!");
		}
		else {
			blackmarketYaml = YamlConfiguration.loadConfiguration(blackmarketFile);
		}
		
	}
	
	public void unloadFiles() {
		shopfiles = null;
		moneyfile = null;
		cashyaml = null;
		playerDataFile = null;
		blackmarketFile = null;
		blackmarketYaml = null;
	}
	
	// --------------------------------------- //
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		loadFiles();
			
		if (cmd.getName().equalsIgnoreCase("bank")) {
			if (args.length == 0) {
				if (sender instanceof Player)
				{
					Player player = (Player) sender;
					player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "$" + ChatColor.RESET + ChatColor.GRAY + " You currently have: " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + player.getUniqueId().toString()) + " Rhines" + ChatColor.GRAY + ".");
					unloadFiles();
					return true;
				}
				else
				{
					sender.sendMessage("Only players can check their own bal.");
					unloadFiles();
					return false;
				}
				
			}
			else if (args.length == 1) {
				String id;
				if (Bukkit.getPlayer(args[0]) != null) {
					id = Bukkit.getPlayer(args[0]).getUniqueId().toString();
				} else {
					id = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
				}
				sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "$ " + ChatColor.RESET + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " currently has: " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + id) + " Rhines" + ChatColor.GRAY + ".");
				unloadFiles();
				return true;
			}
		} 
		else if (cmd.getName().equalsIgnoreCase("pay")) {
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				
				if (args.length == 2) {
					if (Bukkit.getPlayer(args[0]) == null) {
						player.sendMessage(args[0] + ChatColor.GRAY + " is not online!");
						unloadFiles();
						return false;
					}
					if (!args[1].matches("([1-9]\\d*)")) {
						player.sendMessage(ChatColor.GRAY + "'" + args[1] + "' is not a valid amount.");
						unloadFiles();
						return false;
					}
					String idreceiver = Bukkit.getPlayer(args[0]).getUniqueId().toString();
					String idsender = player.getUniqueId().toString();
					int money = Integer.parseInt(args[1]);
					
					if ((cashyaml.getInt("PlayerData." + idsender) - money) < 0) {
						player.sendMessage(ChatColor.RED + "You don't have enough money to pay that much!");
						unloadFiles();
						return false;
					}
					cashyaml.set("PlayerData." + idreceiver, cashyaml.getInt("PlayerData." + idreceiver) + money);
					cashyaml.set("PlayerData." + idsender, cashyaml.getInt("PlayerData." + idsender) - money);
					saveyaml(cashyaml, moneyfile);
					player.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + money + " Rhines" + ChatColor.GRAY + " to " + ChatColor.YELLOW + Bukkit.getPlayer(args[0]).getPlayerListName() + ChatColor.GRAY + ".");
					Bukkit.getPlayer(args[0]).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + money + " Rhines" + ChatColor.GRAY + " from " + ChatColor.YELLOW + player.getPlayerListName() + ChatColor.GRAY + ".");
					unloadFiles();
					return true;
				}
				else {
					player.sendMessage(ChatColor.RED + "Incorrect use.");
					player.sendMessage(ChatColor.RED + "/pay <Player name> <amount>");
					unloadFiles();
					return false;
				}
			}
			else 
				sender.sendMessage("Only players can check their own bal.");
		}
		else if (cmd.getName().equalsIgnoreCase("givecash")) {
			if (sender.isOp()) {
				if (args.length == 2) {
					String id;
					if (Bukkit.getPlayer(args[0]) != null) {
						id = Bukkit.getPlayer(args[0]).getUniqueId().toString();
					} else {
						id = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
					}
					if (!args[1].matches("(-?[1-9]\\d*)")) {
						sender.sendMessage(ChatColor.GRAY + "'" + args[1] + "' is not a valid balance.");
						unloadFiles();
						return false;
					}
					
					int money = cashyaml.getInt("PlayerData." + id) + (Integer.parseInt(args[1]));
					if (money < 0) money = 0;
					
					cashyaml.set("PlayerData." + id, money);
					saveyaml(cashyaml, moneyfile);
					sender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.GRAY + " now has " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + id) + " Rhines" + ChatColor.GRAY + ".");
					unloadFiles();
					return true;
				}
				else {
					sender.sendMessage(ChatColor.RED + "Incorrect use.");
					sender.sendMessage(ChatColor.RED + "/givecash <Player name> <amount>");
					unloadFiles();
					return false;
				}
			}
			else 
				sender.sendMessage(ChatColor.RED+ "You don't have permission to do that!");
		}
		else if (cmd.getName().equalsIgnoreCase("setcash")) {
			if (sender.isOp()) {
				if (args.length == 2) {
					String id;
					if (Bukkit.getPlayer(args[0]) != null) {
						id = Bukkit.getPlayer(args[0]).getUniqueId().toString();
					} else {
						id = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
					}
					if (!args[1].matches("(0|[1-9]\\d*)")) {
						sender.sendMessage(ChatColor.GRAY + "'" + args[1] + "' is not a valid balance.");
						unloadFiles();
						return false;
					}
					
					cashyaml.set("PlayerData." + id, Integer.parseInt(args[1]));
					saveyaml(cashyaml, moneyfile);
					sender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.GRAY + " now has " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + id) + " Rhines" + ChatColor.GRAY + ".");
					unloadFiles();
					return true;
				}
				else {
					sender.sendMessage(ChatColor.RED + "Incorrect use.");
					sender.sendMessage(ChatColor.RED + "/setcash <Player name> <amount>");
					unloadFiles();
					return false;
				}
			}
			else 
				sender.sendMessage(ChatColor.RED+ "You don't have permission to do that!");
		}
		else if (cmd.getName().equalsIgnoreCase("baltop")) {
			//Map<String, Integer> top5 = new HashMap<String, Integer>();
			List<UUID> pointers = new ArrayList<UUID>();
			
			UUID uuid;
			for (String uuidstring : cashyaml.getConfigurationSection("PlayerData").getKeys(false)) {
				uuid = UUID.fromString(uuidstring);
				if (pointers.size() < 5) {
					pointers.add(uuid);
				}
				else {
					int currentBal = cashyaml.getInt("PlayerData." + uuid);
					for (UUID pointeruuid : pointers) {
						if (currentBal > cashyaml.getInt("PlayerData." + pointeruuid)) {
							pointers.add(uuid);
							UUID lowestpointeruuid = pointeruuid;
							for (UUID pointeruuid2 : pointers) {
								if (cashyaml.getInt("PlayerData." + pointeruuid2) < (cashyaml.getInt("PlayerData." + lowestpointeruuid))) {
									lowestpointeruuid = pointeruuid2;
								}
							}
							pointers.remove(lowestpointeruuid);
							break;
						}
					}
				}
			}
			sender.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "Top players:");
			
			UUID playeruuid = null;
			int size = pointers.size();
			for (int j = 1; j <= size; j++) {
				int max = 0;
				
				for (int i = 0; i < pointers.size(); i++) {
					if (cashyaml.getInt("PlayerData." + pointers.get(i)) > max) {
						max = cashyaml.getInt("PlayerData." + pointers.get(i));
						playeruuid = (pointers.get(i));
					}
				}
				
				pointers.remove(playeruuid);
				if (Bukkit.getPlayer(playeruuid) != null) sender.sendMessage(ChatColor.GOLD + "" + j + ") " + ChatColor.YELLOW + Bukkit.getPlayer(playeruuid).getName() + ChatColor.GRAY + " - " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + playeruuid) + " Rhines");
				else  sender.sendMessage(ChatColor.GOLD + "" + j + ") " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(playeruuid).getName() + ChatColor.GRAY + " - " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + playeruuid) + " Rhines");
			}
		}
		unloadFiles();	
		return false;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadFiles();
		String id = ((Player) event.getPlayer()).getUniqueId().toString();
		if (!cashyaml.getStringList("Players").contains(id)) {
			List<String> ps = cashyaml.getStringList("Players");
			ps.add(id);
			cashyaml.set("Players", ps);
			cashyaml.createSection("PlayerData." + id);
			cashyaml.set("PlayerData." + id, 100);
			saveyaml(cashyaml, moneyfile);
		}
		unloadFiles();
	}
	
	
	// Golden coins
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (event.getHand().equals(EquipmentSlot.HAND)) {
				if (event.getItem() != null && event.getItem().getType() == Material.GOLD_NUGGET) {
					ItemMeta meta = event.getItem().getItemMeta();
					if (meta.getLore() != null) 
					{
						for (String lorerow : meta.getLore())
						{
							if (lorerow.contains("Right click to add to your balance.")) 
							{
								loadFiles();
								Player player = (Player) event.getPlayer();
								int moneyToAdd = Integer.parseInt(meta.getLore().get(0).substring(8).replaceFirst(" rhines", "")) * event.getItem().getAmount();
								event.getItem().setAmount(0);
								cashyaml.set("PlayerData." + player.getUniqueId().toString(), cashyaml.getInt("PlayerData." + player.getUniqueId().toString()) + moneyToAdd);
								saveyaml(cashyaml, moneyfile);
								player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "$" + ChatColor.RESET + ChatColor.GRAY + " You currently have: " + ChatColor.GOLD + cashyaml.getInt("PlayerData." + player.getUniqueId().toString()) + " Rhines" + ChatColor.GRAY + ".");
								unloadFiles();
								return;
							}
						}
					}
				}
			}	
		}
	}
		
	
	
	
	// -------------------Shop Creation--------------------- //

	
	@EventHandler(ignoreCancelled = true)
	public void onSignShopPlace(SignChangeEvent sign) {
		UUID playerUUID = sign.getPlayer().getUniqueId();
		
		if(sign.getLine(0).equalsIgnoreCase("-[Buy]-")) 
		{
			handleSigns(sign, playerUUID, 1);
		} 
		
		else if (sign.getLine(0).equalsIgnoreCase("-[Sell]-")) 
		{
			handleSigns(sign, playerUUID, 2);
		} 
		
		else if (sign.getLine(0).equalsIgnoreCase("=[Buy]="))
		{
			if (sign.getPlayer().isOp())
				handleSigns(sign, playerUUID, 3);
			else
				sign.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to make an admin shop!");
		} 
		
		else if (sign.getLine(0).equalsIgnoreCase("=[Sell]=")) 
		{
			if (sign.getPlayer().isOp())
				handleSigns(sign, playerUUID, 4);
			else
				sign.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to make an admin shop!");
		}
	}
	
	private void handleSigns(SignChangeEvent sign, UUID playerUUID, int buyOrSell) {	
		// Check if price is well formatted.
		String price = sign.getLine(3);
		if (!price.matches("([1-9]\\d*)")) {
			if (buyOrSell == 1)
				sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "-[Buy]-");
			else if (buyOrSell == 2)
				sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "-[Sell]-");
			else if (buyOrSell == 3)
				sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "=[Buy]=");
			else if (buyOrSell == 4)
				sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "=[Sell]=");
			sign.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + price);
			
			try {
				Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "Couldn't create shop!");
				Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "Line 4: '" + price + "' is not a valid price! (only include numbers)");
			} catch (Exception ingnored) {}
			return;
		}
		
		// Price is valid so make shop.
		createShop(sign, Bukkit.getPlayer(playerUUID), price);
		if (buyOrSell == 1)
			sign.setLine(0, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "-[Buy]-" + ChatColor.RESET);
		else if (buyOrSell == 2)
			sign.setLine(0, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "-[Sell]-" + ChatColor.RESET);
		else if (buyOrSell == 3)
			sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=[Buy]=" + ChatColor.RESET);
		else if (buyOrSell == 4)
			sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=[Sell]=" + ChatColor.RESET);
		sign.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + price);
	}
	

	
	private void createShop(SignChangeEvent sign, Player player, String price) {
		Inventory inv;
		inv = Bukkit.createInventory(player, InventoryType.HOPPER, "   Specify what and how much:");
		player.openInventory(inv);
		invs.put(player.getName(), inv);
		signs.put(player.getName(), sign);
	}


	private void cancelShop(Player player) {
		if (!player.getGameMode().name().equalsIgnoreCase("Creative")) {
			ItemStack item = new ItemStack(signs.get(player.getName()).getBlock().getBlockData().getMaterial(), 1);
			if (item.getType() == Material.OAK_WALL_SIGN) item.setType(Material.OAK_SIGN);
			if (item.getType() == Material.BIRCH_WALL_SIGN) item.setType(Material.BIRCH_SIGN);
			if (item.getType() == Material.SPRUCE_WALL_SIGN) item.setType(Material.SPRUCE_SIGN);
			if (item.getType() == Material.JUNGLE_WALL_SIGN) item.setType(Material.JUNGLE_SIGN);
			if (item.getType() == Material.ACACIA_WALL_SIGN) item.setType(Material.ACACIA_SIGN);
			if (item.getType() == Material.DARK_OAK_WALL_SIGN) item.setType(Material.DARK_OAK_SIGN);
			
			player.getInventory().addItem(item);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1f);
		}
		signs.get(player.getName()).getBlock().setType(Material.AIR);
		invs.remove(player.getName());
		signs.remove(player.getName());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInvClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		
		if (invs.containsKey(player.getName()) && signs.containsKey(player.getName())) {
			boolean cancel = true;
			int count = 0;
			for (int i = 0; i < 5; i++) {
	    		if (invs.get(player.getName()).getItem(i) != null) {
	    			count++;
	    			cancel = false;
	    		}
			}
			if (cancel) {
				player.sendMessage(ChatColor.RED + "You canceled shop creation.");
				cancelShop(player);
				return;
			}
			else {
				if (count > 1) {
					player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY + " You can only specify one itemstack, you specified " + count + ".");
					for (int i = 0; i < invs.get(player.getName()).getSize(); i++) {
			    		if (invs.get(player.getName()).getItem(i) != null) {
			    			player.getInventory().addItem(invs.get(player.getName()).getItem(i));
			    		}
					}
					cancelShop(player);
					return;
				}
				String code = "";
				code += (signs.get(player.getName()).getBlock().getLocation().getWorld().getName() + "_");
				code += (signs.get(player.getName()).getBlock().getLocation().getBlockX() + "_");
				code += (signs.get(player.getName()).getBlock().getLocation().getBlockY() + "_");
				code += (signs.get(player.getName()).getBlock().getLocation().getBlockZ() + "");
				File shopfile = new File(getDataFolder(), "ShopData" + File.separatorChar + code + ".yml");
				YamlConfiguration yaml = null;
				if(!shopfile.exists()){
		            try {
						shopfile.createNewFile();
						yaml = YamlConfiguration.loadConfiguration(shopfile);
						saveyaml(yaml, shopfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        } else {
		        	yaml = YamlConfiguration.loadConfiguration(shopfile);
		        }
				
				// Safe admin shop or not.
				yaml.createSection("AdminShop");
				if (signs.get(player.getName()).getLine(0).contains("-[Buy]-")
						|| signs.get(player.getName()).getLine(0).contains("-[Sell]-"))
					yaml.set("AdminShop", 1);
				else if ((signs.get(player.getName()).getLine(0).contains("=[Buy]=")
						|| signs.get(player.getName()).getLine(0).contains("=[Sell]=")))
					yaml.set("AdminShop", 2);
				else
					yaml.set("AdminShop", 0);
				
				// Safe shop Owner.
				yaml.createSection("Player");
				yaml.set("Player", player.getUniqueId().toString());
				
				// Safe Location.
				Location loc = signs.get(player.getName()).getBlock().getLocation();
				yaml.createSection("Location");
				yaml.createSection("Location.x");
				yaml.createSection("Location.y");
				yaml.createSection("Location.z");
				yaml.createSection("Location.world");
				yaml.set("Location.x", loc.getBlockX());
				yaml.set("Location.y", loc.getBlockY());
				yaml.set("Location.z", loc.getBlockZ());
				yaml.set("Location.world", loc.getWorld().getName());
				
				// Safe inventory.
				Inventory futureStockInv = Bukkit.createInventory(null, 27);
				Inventory inv = event.getInventory();
				List<ItemStack> content = new ArrayList<ItemStack>();
				List<ItemStack> shop = new ArrayList<ItemStack>();
				yaml.createSection("Inventory");
				yaml.createSection("Inventory.content");
				yaml.createSection("Inventory.shop");
				for (ItemStack itemstack : futureStockInv.getStorageContents()) // add null values in yml list
				{
					content.add(itemstack);
				}
				for (int i = 0; i < inv.getSize(); i++) // set shop item
				{
					if (inv.getStorageContents()[i] != null)
					{
						shop.add(inv.getStorageContents()[i]);
						content.set(0, inv.getStorageContents()[i]);
					}
				}
				
				yaml.set("Inventory.content", content);
				yaml.set("Inventory.shop", shop);
				
				saveyaml(yaml, shopfile);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
			}
			signs.remove(player.getName());
			invs.remove(player.getName());
			
			player.sendMessage(ChatColor.GREEN + "Sign shop created succesfully!");
		}
		else if (editInvs.containsKey(event.getInventory())) {
			YamlConfiguration yaml;
			Inventory inv = event.getInventory();
			List<ItemStack> content = new ArrayList<ItemStack>();
			yaml = YamlConfiguration.loadConfiguration(editInvs.get(inv));
			
			for (ItemStack itemstack : inv.getContents())
				content.add(itemstack);
			yaml.set("Inventory.content", content);
			saveyaml(yaml, editInvs.get(inv));
		
			editInvs.remove(event.getInventory());
		}
	}
	
	public void saveyaml(YamlConfiguration yaml, File file) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// -------------------Shop Break--------------------- //
	
	@EventHandler(ignoreCancelled = true)
	public void onSignBreak(BlockBreakEvent event) {
		Player player = (Player) event.getPlayer();
		
		if (allSigns.contains(event.getBlock().getType())) {
			Location loc = event.getBlock().getLocation();
			loadFiles();
			
			File shopfile = new File(getDataFolder(), "ShopData" + File.separatorChar + loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ() + ".yml");
			YamlConfiguration yaml;
			if(!shopfile.exists()){
				unloadFiles();
				return;
	        } else {
	        	yaml = YamlConfiguration.loadConfiguration(shopfile);
	        }
			
			if (yaml.getString("Player").equals(player.getUniqueId().toString()) || player.isOp() || player.getGameMode().name().equalsIgnoreCase("Creative")) {
				for (Object item : yaml.getList("Inventory.content")) {
					if (item != null) loc.getWorld().dropItemNaturally(loc, (ItemStack) item);
				}
				loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
				deleteDirectory(shopfile);
			} else {
				event.setCancelled(true);
				player.sendMessage(ChatColor.GRAY + "You can't break a shop that you don't own!");
			}
			
			unloadFiles();
		}
	}
	
	/*@EventHandler(ignoreCancelled = true)
	public void signDetachCheck(BlockPhysicsEvent event) {
		if (allSigns.contains(event.getBlock().getType())) {
			Location loc = event.getBlock().getLocation();
			loadFiles();
			for (File f : shopfiles.listFiles()) {
				YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
						
				if (loc.getBlockX() == yaml.getInt("Location.x")
					&& loc.getBlockY() == yaml.getInt("Location.y")
					&& loc.getBlockZ() == yaml.getInt("Location.z")
					&& loc.getWorld().getName().equals(yaml.getString("Location.world"))) {
					
					if (event.getBlock().getState().getBlockData() instanceof WallSign) {
						WallSign signData = (WallSign) event.getBlock().getState().getBlockData();
						BlockFace attached = signData.getFacing().getOppositeFace();
						if (event.getBlock().getRelative(attached).getType() == Material.AIR) {
							for (Object item : yaml.getList("Inventory.content")) {
								if (item != null) loc.getWorld().dropItemNaturally(loc, (ItemStack) item);
							}
							loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
							deleteDirectory(f);
						}
					} else {
						if (loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType() == Material.AIR) {
							for (Object item : yaml.getList("Inventory.content")) {
								if (item != null) loc.getWorld().dropItemNaturally(loc, (ItemStack) item);
							}
							loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
							deleteDirectory(f);
						}
					}
				}
			}
			unloadFiles();
		}
	}*/
	
	static private boolean deleteDirectory(File path) {
	    if (path.exists()) {
	        File[] files = path.listFiles();
	        if (files != null) {
		        for (int i = 0; i < files.length; i++) {
		            if (files[i].isDirectory()) {
		                deleteDirectory(files[i]);
		            } else {
		                files[i].delete();
		            }
		        }
	        }
	    }
	    return (path.delete());
	}
	
	//=====================================================//
	
	
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerClickItemInInv(InventoryClickEvent event) {
		String title = event.getView().getTitle();
		int slot = event.getSlot();
		
		if (title.contains("FTC Shop")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			
			if (slot == 11) {
				event.setCancelled(true);
				this.mainItemShop = new MainItemShop(this);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(mainItemShop.getInv());
				this.mainItemShop = null;
				return;
			}
			else if (slot == 15) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				//this.mainServerShop = new MainServerShop(this);
				//player.openInventory(mainServerShop.getInv());
				player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.YELLOW + "You can visit the webshop here:");
				player.sendMessage(ChatColor.AQUA + "https://forthecrown.buycraft.net/");
				player.closeInventory();
				//this.mainServerShop = null;
				return;
			}
			else {
				event.setCancelled(true);
				return;
			}
		}
		else if (title.contains("Server Shop")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			
			if (slot == 11) {
				event.setCancelled(true);
				this.ranks = new Ranks(this);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(ranks.getInv());
				this.ranks = null;
				return;
			}
			else if (slot == 13) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				this.heads = new Heads(this);
				player.openInventory(heads.getInv());
				this.heads = null;
				return;
			}
			else if (slot == 15) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				this.regions = new Regions(this);
				player.openInventory(regions.getInv());
				this.regions = null;
				return;
			}
			else {
				event.setCancelled(true);
				return;
			}
		}
		else if (title.contains("Ranks")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			
			if (slot >= 10 && slot <= 16) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.YELLOW + "You can purchase ranks here:");
				player.sendMessage(ChatColor.AQUA + "https://forthecrown.buycraft.net/");
				player.closeInventory();
				return;
			}
			else if (slot == 0) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				this.mainServerShop = new MainServerShop(this);
				player.openInventory(mainServerShop.getInv());
				this.mainServerShop = null;
				return;
			}
			else {
				event.setCancelled(true);
				return;
			}
		}
		else if (title.contains("Heads")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			
			if (slot == 12 || slot == 14) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.YELLOW + "You can purchase heads here:");
				player.sendMessage(ChatColor.AQUA + "https://forthecrown.buycraft.net/");
				player.closeInventory();
				return;
			}
			else if (slot == 0) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				this.mainServerShop = new MainServerShop(this);
				player.openInventory(mainServerShop.getInv());
				this.mainServerShop = null;
				return;
			}
			else {
				event.setCancelled(true);
				return;
			}
		}
		else if (title.contains("Regions")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			
			if (slot == 12 || slot == 14) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.YELLOW + "You can purchase tickets here:");
				player.sendMessage(ChatColor.AQUA + "https://forthecrown.buycraft.net/");
				player.closeInventory();
				return;
			}
			else if (slot == 0) {
				event.setCancelled(true);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				this.mainServerShop = new MainServerShop(this);
				player.openInventory(mainServerShop.getInv());
				this.mainServerShop = null;
				return;
			}
			else {
				event.setCancelled(true);
				return;
			}
		}
		else if (title.contains("Item Shop")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			event.setCancelled(true);
			
			if (slot == 11) {
				openCorrectInv(player, 0);
				if (invTypeSetting.replace(player.getName(), 0) == null) invTypeSetting.put(player.getName(), 0);
				return;
			}
			else if (slot == 13) {
				openCorrectInv(player, 1);
				if (invTypeSetting.replace(player.getName(), 1) == null) invTypeSetting.put(player.getName(), 1);
				return;
			}
			else if (slot == 15) {
				openCorrectInv(player, 2);
				if (invTypeSetting.replace(player.getName(), 2) == null) invTypeSetting.put(player.getName(), 2);
				return;
			}
			else {
				return;
			}
		}
		else if (title.contains("Selling Items") || title.contains("Buying Items")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			event.setCancelled(true);
			
			if (slot == 0) {
				this.mainItemShop = new MainItemShop(this);
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				player.openInventory(mainItemShop.getInv());
				this.mainItemShop = null;
				return;
			}
			if (slot == 17) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				amountSetting.put(player.getName(), "1");
				sellShopF1 = new SellShop(this, trade.getInv(), 0, "1", player.getUniqueId().toString());
				sellShopM1 = new SellShop(this, trade.getInv(), 1, "1", player.getUniqueId().toString());
				sellShopD1 = new SellShop(this, trade.getInv(), 2, "1", player.getUniqueId().toString());
				if (invTypeSetting.get(player.getName()) == 0) player.openInventory(sellShopF1.getInv());
				else if (invTypeSetting.get(player.getName()) == 1) player.openInventory(sellShopM1.getInv());
				else if (invTypeSetting.get(player.getName()) == 2) player.openInventory(sellShopD1.getInv());
				sellShopF1 = null;
				sellShopM1 = null;
				sellShopD1 = null;
			}
			else if (slot == 26) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				amountSetting.put(player.getName(), "16");
				sellShopF16 = new SellShop(this, trade.getInv(), 0, "16", player.getUniqueId().toString());
				sellShopM16 = new SellShop(this, trade.getInv(), 1, "16", player.getUniqueId().toString());
				sellShopD16 = new SellShop(this, trade.getInv(), 2, "16", player.getUniqueId().toString());
				if (invTypeSetting.get(player.getName()) == 0) player.openInventory(sellShopF16.getInv());
				else if (invTypeSetting.get(player.getName()) == 1) player.openInventory(sellShopM16.getInv());
				else if (invTypeSetting.get(player.getName()) == 2) player.openInventory(sellShopD16.getInv());
				sellShopF16 = null;
				sellShopM16 = null;
				sellShopD16 = null;
				
			}
			else if (slot == 35) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				amountSetting.put(player.getName(), "64");
				sellShopF64 = new SellShop(this, trade.getInv(), 0, "64", player.getUniqueId().toString());
				sellShopM64 = new SellShop(this, trade.getInv(), 1, "64", player.getUniqueId().toString());
				sellShopD64 = new SellShop(this, trade.getInv(), 2, "64", player.getUniqueId().toString());
				if (invTypeSetting.get(player.getName()) == 0) player.openInventory(sellShopF64.getInv());
				else if (invTypeSetting.get(player.getName()) == 1) player.openInventory(sellShopM64.getInv());
				else if (invTypeSetting.get(player.getName()) == 2) player.openInventory(sellShopD64.getInv());
				sellShopF64 = null;
				sellShopM64 = null;
				sellShopD64 = null;
			}
			else if (slot == 44) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				amountSetting.put(player.getName(), "all");
				sellShopFall = new SellShop(this, trade.getInv(), 0, "all", player.getUniqueId().toString());
				sellShopMall = new SellShop(this, trade.getInv(), 1, "all", player.getUniqueId().toString());
				sellShopDall = new SellShop(this, trade.getInv(), 2, "all", player.getUniqueId().toString());
				if (invTypeSetting.get(player.getName()) == 0) player.openInventory(sellShopFall.getInv());
				else if (invTypeSetting.get(player.getName()) == 1) player.openInventory(sellShopMall.getInv());
				else if (invTypeSetting.get(player.getName()) == 2) player.openInventory(sellShopDall.getInv());
				sellShopFall = null;
				sellShopMall = null;
				sellShopDall = null;
			}
			else if ((slot > 9 && slot < 17) || (slot > 18 && slot < 26) || (slot > 27 && slot < 35) || (slot > 36 && slot < 44)) {
				if (event.getInventory().getItem(slot) == null) return;
				if (!onCooldown.contains(player.getName())) sellItems(player, event.getView().getTopInventory().getItem(slot), event.getInventory().getItem(slot).getItemMeta().getLore().get(2).substring(33), false);
			}
		}
		else if (title.contains("Black Market: Enchants"))
		{
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			event.setCancelled(true);
			
			if (slot == 13) {
				if (event.getInventory().getItem(slot) == null || event.getInventory().getItem(slot).getType() != Material.ENCHANTED_BOOK || (!event.getInventory().getItem(slot).getItemMeta().hasLore())) return;
				tryBuyEnchant(player);
			}
		}
		else if (title.contains("Black Market: ")) {
			if (event.getClickedInventory() instanceof PlayerInventory) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getWhoClicked();
			event.setCancelled(true);
			
			if (slot > 9 && slot < 17) {
				if (event.getInventory().getItem(slot) == null) return;
				if (!onCooldown.contains(player.getName())) sellItems(player, event.getView().getTopInventory().getItem(slot), "all", true);
			}
			
		}
		else if (title.contains("Parrot Shop"))
		{
			event.setCancelled(true);
			if (event.getClickedInventory() instanceof PlayerInventory) return;
			if (slot < 11 || slot > 15) return;
			if (event.getCurrentItem() == null) return;
			
			Player player = (Player) event.getWhoClicked();
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			
			List<String> pets = getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + player.getUniqueId().toString() + ".Pets");
			if (pets == null) pets = new ArrayList<>();
			
			switch (event.getCurrentItem().getType()) {
			case GRAY_WOOL:
				if (pets.contains("gray_parrot")) {
					player.sendMessage(ChatColor.GRAY + "You already own this type. Try /parrot gray.");
					sendParrotInfo(player);
				}
				else tryBuyParrot(player, 50000, "gray");
				return;
			case GREEN_WOOL:
				if (pets.contains("green_parrot"))  {
					player.sendMessage(ChatColor.GRAY + "You already own this type. Try /parrot green.");
					sendParrotInfo(player);
				}
				else tryBuyParrot(player, 50000, "green");
				return;
			case BLUE_WOOL:
				if (pets.contains("blue_parrot"))  {
					player.sendMessage(ChatColor.GRAY + "You already own this type. Try /parrot blue.");
					sendParrotInfo(player);
				}
				else tryBuyParrot(player, 100000, "blue");
				return;
			case RED_WOOL:
				if (player.hasPermission("ftc.donator2")) {
					player.sendMessage(ChatColor.GRAY + "You can do /parrot red to spawn it.");
					sendParrotInfo(player);
				}
				else player.sendMessage(ChatColor.GRAY + "You can't get this parrot at the moment.");
				return;
			case LIGHT_BLUE_WOOL:
				if (player.hasPermission("ftc.donator3")) {
					player.sendMessage(ChatColor.GRAY + "You can do /parrot aqua to spawn it.");
					sendParrotInfo(player);
				}
				else player.sendMessage(ChatColor.GRAY + "You can't get this parrot at the moment.");
				return;
			default:
				break;
			}
		}
			
		
	}

	@SuppressWarnings("deprecation")
	private void tryBuyEnchant(Player player) {
		loadFiles();
		int price = blackmarketYaml.getInt("Price_Per_Item.Enchants." + blackmarketYaml.getString("ChosenEnchant"));
		File playerFile = new File(playerDataFile, player.getUniqueId().toString() + ".yml");
		YamlConfiguration playerFileYaml = YamlConfiguration.loadConfiguration(playerFile);
		if (playerFileYaml.getBoolean("Bought_Enchant") == true)
		{
			player.sendMessage(ChatColor.RED + "You've already bought this item.");
			return;
		}
		if (!enoughMoney(player.getUniqueId().toString(), price)) 
		{
			player.sendMessage(ChatColor.RED + "You don't have enough money to pay for that!");
			return;
		}
		if (checkIfSpaceInInventory(player.getInventory()) == false) 
		{
			player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "$" + ChatColor.RESET + ChatColor.GRAY + " You don't have a free spot in your inventory!");
			return;
		}
		player.sendMessage(ChatColor.GRAY + "You've bought the enchanted book.");
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
		meta.addStoredEnchant(Enchantment.getByName(blackmarketYaml.getString("ChosenEnchant")), Enchantment.getByName(blackmarketYaml.getString("ChosenEnchant")).getMaxLevel() + 1, true);
		book.setItemMeta(meta);
		player.getInventory().addItem(book);
		playerFileYaml.set("Bought_Enchant", true);
		saveyaml(playerFileYaml, playerFile);
		payMoneyForPurchase(player.getUniqueId().toString(), price);
		unloadFiles();
	}
	
	public boolean checkIfSpaceInInventory(Inventory inv) {
		int size;
		if (inv instanceof PlayerInventory) size = 36;
		else size = inv.getSize();
		
    	for (int i = 0; i < size; i++) 
    	{
    		if (inv.getItem(i) == null)
    			return true;
    	}
    	return false;
	}

	private void sendParrotInfo(Player player)
	{
		player.sendMessage(ChatColor.WHITE + "/parrot [gray/green/auqa/blue/red]");
		player.sendMessage(ChatColor.WHITE + "/parrot silent " + ChatColor.GRAY + "to silence your parrot.");
		player.sendMessage(ChatColor.WHITE + "/parrot " + ChatColor.GRAY + "to toggle sound.");
	}

	private void tryBuyParrot(Player player, int price, String color) 
	{
		loadFiles();
		if (!enoughMoney(player.getUniqueId().toString(), price)) 
		{
			player.sendMessage(ChatColor.RED + "You don't have enough money to pay for that!");
			return;
		}
		player.sendMessage(ChatColor.GRAY + "You've bought the " + color + " parrot. Try /parrot " + color + ".");
		Bukkit.dispatchCommand(getServer().getConsoleSender(), "addpet " + player.getName() + " " + color + "_parrot");
		payMoneyForPurchase(player.getUniqueId().toString(), price);
		unloadFiles();
	}
	
	private boolean enoughMoney(String playerUUID, int price)
	{
		return ((cashyaml.getInt("PlayerData." + playerUUID) - price) >= 0);
	}

	private void payMoneyForPurchase(String playerUUID, int price) 
	{
		cashyaml.set("PlayerData." + playerUUID, cashyaml.getInt("PlayerData." +  playerUUID) - price);
		saveyaml(cashyaml, moneyfile);	
	}

	private void openCorrectInv(Player player, int invType) {
		player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

		if (invType == 0) {
			sellShopFall = new SellShop(this, trade.getInv(), 0, "all", player.getUniqueId().toString());
			sellShopF64 = new SellShop(this, trade.getInv(), 0, "64", player.getUniqueId().toString());
			sellShopF1 = new SellShop(this, trade.getInv(), 0, "1", player.getUniqueId().toString());
			sellShopF16 = new SellShop(this, trade.getInv(), 0, "16", player.getUniqueId().toString());
			if (!amountSetting.containsKey(player.getName())) {
				player.openInventory(sellShopFall.getInv());
				amountSetting.put(player.getName(), "all");
			}
			else {
				if (amountSetting.get(player.getName()) == "all") player.openInventory(sellShopFall.getInv());
				else if (amountSetting.get(player.getName()) == "64") player.openInventory(sellShopF64.getInv());
				else if (amountSetting.get(player.getName()) == "1") player.openInventory(sellShopF1.getInv());
				else if (amountSetting.get(player.getName()) == "16") player.openInventory(sellShopF16.getInv());
			}
			sellShopFall = null;
			sellShopF64 = null;
			sellShopF1 = null;
			sellShopF16 = null;
		}
		else if (invType == 1) {
			sellShopMall = new SellShop(this, trade.getInv(), 1, "all", player.getUniqueId().toString());
			sellShopM64 = new SellShop(this, trade.getInv(), 1, "64", player.getUniqueId().toString());
			sellShopM1 = new SellShop(this, trade.getInv(), 1, "1", player.getUniqueId().toString());
			sellShopM16 = new SellShop(this, trade.getInv(), 1, "16", player.getUniqueId().toString());
			if (!amountSetting.containsKey(player.getName())) {
				player.openInventory(sellShopMall.getInv());
				amountSetting.put(player.getName(), "all");
			}
			else {
				if (amountSetting.get(player.getName()) == "all") player.openInventory(sellShopMall.getInv());
				else if (amountSetting.get(player.getName()) == "64") player.openInventory(sellShopM64.getInv());
				else if (amountSetting.get(player.getName()) == "1") player.openInventory(sellShopM1.getInv());
				else if (amountSetting.get(player.getName()) == "16") player.openInventory(sellShopM16.getInv());
			}
			sellShopMall = null;
			sellShopM64 = null;
			sellShopM1 = null;
			sellShopM16 = null;
		}
		else if (invType == 2) {
			sellShopDall = new SellShop(this, trade.getInv(), 2, "all", player.getUniqueId().toString());
			sellShopD64 = new SellShop(this, trade.getInv(), 2, "64", player.getUniqueId().toString());
			sellShopD1 = new SellShop(this, trade.getInv(), 2, "1", player.getUniqueId().toString());
			sellShopD16 = new SellShop(this, trade.getInv(), 2, "16", player.getUniqueId().toString());
			if (!amountSetting.containsKey(player.getName())) {
				player.openInventory(sellShopDall.getInv());
				amountSetting.put(player.getName(), "all");
			}
			else {
				if (amountSetting.get(player.getName()) == "all") player.openInventory(sellShopDall.getInv());
				else if (amountSetting.get(player.getName()) == "64") player.openInventory(sellShopD64.getInv());
				else if (amountSetting.get(player.getName()) == "1") player.openInventory(sellShopD1.getInv());
				else if (amountSetting.get(player.getName()) == "16") player.openInventory(sellShopD16.getInv());
			}
			sellShopDall = null;
			sellShopD64 = null;
			sellShopD1 = null;
			sellShopD16 = null;
		}
		return;
	}
	
	
	private void sellItems(Player player, ItemStack item, String amount, Boolean isBMsale) {
		loadFiles();
		if (amount.contains("all")) {
			if (isBMsale)
				sellBMitems(player, item);
			else
				sellAmountOfItems(player, item, 9999);
		}
		else if (amount.contains("64")) {
			sellAmountOfItems(player, item, 64);
		}
		else if (amount.contains("16")) {
			sellAmountOfItems(player, item, 16);
		}
		else if (amount.contains("1")) {
			sellAmountOfItems(player, item, 1);
		}
		unloadFiles();
	}

	private void sellBMitems(Player player, ItemStack item) {
		YamlConfiguration playerFileYaml = YamlConfiguration.loadConfiguration(getFile(player.getUniqueId().toString()));
		if (playerFileYaml.getInt("MoneyEarnedInBM") > 50000)
		{
			player.sendMessage(ChatColor.RED + "$ " + ChatColor.GRAY + "You can't sell more items in the blackmarket today.");
			return;
		}
		
		startCooldown(player);
		int amountOfItemsFoundInInv = 0;
		
		for (ItemStack invItem : player.getInventory().getContents()) {
			if (invItem != null) {
				if (invItem.getType() == item.getType()) {
					amountOfItemsFoundInInv = amountOfItemsFoundInInv + invItem.getAmount();
					invItem.setAmount(0);
				}
			}
		}
		
		if (amountOfItemsFoundInInv == 0) 
		{
			player.sendMessage(ChatColor.RED + "$ " + ChatColor.GRAY + "You don't have " + item.getType().name().replace("_", " ").toLowerCase() + " in your inventory.");
			return;
		}
		
		String section = findSection(blackmarketYaml, item.getType().toString());
		int price = getPrice(blackmarketYaml, section);
		int moneyToGet = price*amountOfItemsFoundInInv;
		
		player.sendMessage(ChatColor.GRAY + "You've sold " + ChatColor.YELLOW + (item.getType().toString().replace("_", " ").toLowerCase() + ChatColor.GRAY + " for " + ChatColor.GOLD + moneyToGet + " Rhines" + ChatColor.GRAY + "."));
	    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 1f);
		
	    addToBal(player.getUniqueId().toString(), moneyToGet);
		
		this.getServer().getConsoleSender().sendMessage(player.getName() + " sold " + amountOfItemsFoundInInv + " " + item.getType().toString().replace("_", " ").toLowerCase() + " in the black market.");
		
		playerFileYaml.set("MoneyEarnedInBM", playerFileYaml.getInt("MoneyEarnedInBM") + moneyToGet);
		saveyaml(playerFileYaml, getFile(player.getUniqueId().toString()));
	}

	private void sellAmountOfItems(Player player, ItemStack item, int amount) {
		int amountOfItemsFoundInInv = 0;
		startCooldown(player);
		
		YamlConfiguration playerFileYaml = YamlConfiguration.loadConfiguration(getFile(player.getUniqueId().toString()));
		String section = findSection(playerFileYaml, item.getType().toString());
		
		// Price can't be 0.
		int pricePerItem = getPrice(playerFileYaml, section);
		if (pricePerItem == 0)
		{	
			player.sendMessage(ChatColor.RED + "$ " + ChatColor.GRAY + "You've sold too many of these items already.");
			return;
		}
		
		// Amount can't be 0.
		if (amount == 9999) 	// Take all
		{
			for (ItemStack invItem : player.getInventory().getContents()) {
				if (invItem != null) {
					if (invItem.getType() == item.getType()) {
						amountOfItemsFoundInInv = amountOfItemsFoundInInv + invItem.getAmount();
						invItem.setAmount(0);
					}
				}
			}
		}
		else 					// Take specified amount
		{
			for (ItemStack invItem : player.getInventory().getContents()) {
				if (invItem != null) {
					if (invItem.getType() == item.getType()) {
						while(amountOfItemsFoundInInv < amount && invItem.getAmount() > 0) {
							amountOfItemsFoundInInv++;
							invItem.setAmount(invItem.getAmount()-1);
						}
						if (amountOfItemsFoundInInv == amount) break;
					}
				}
			}
		}
		if (amountOfItemsFoundInInv == 0) 
		{
			player.sendMessage(ChatColor.RED + "$ " + ChatColor.GRAY + "You don't have " + item.getType().name().replace("_", " ").toLowerCase() + " in your inventory.");
			return;
		}
		
		// Sell and update moneys.
		int currentMoneyEarned = getCurrentAmountMoneyEarned(playerFileYaml, section);
		int estimatedMoneyToGet = pricePerItem*amountOfItemsFoundInInv;
		int startPrice = getPrice((YamlConfiguration) getConfig(), section);
		doSell(playerFileYaml, section, player, estimatedMoneyToGet, item.getType().name(), amountOfItemsFoundInInv);

		// Change price if needed.
		if (pricePerItem != wowoBigBrain(startPrice, currentMoneyEarned+estimatedMoneyToGet))
		{
			int newPrice = wowoBigBrain(startPrice, currentMoneyEarned+estimatedMoneyToGet);
			player.sendMessage(ChatColor.GRAY + "The price of " + ChatColor.YELLOW + item.getType().name().replace("_", " ").toLowerCase() + ChatColor.GRAY + " has dropped to " + ChatColor.GOLD + newPrice + ChatColor.GRAY + " for you.");
			playerFileYaml.set("Price_Per_Item." + section, newPrice);
			saveyaml(playerFileYaml, getFile(player.getUniqueId().toString()));
			updatePrice(item, newPrice);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.5f, 0.5f);
		}
	}
	


	private void updatePrice(ItemStack item, int newPrice) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore =meta.getLore();
			
		lore.set(0, ChatColor.YELLOW + "Value: " + newPrice + " Rhines per item.");
		lore.set(1, ChatColor.GOLD + "" + newPrice*64 + " Rhines for a stack");
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	private void doSell(YamlConfiguration playerFileYaml, String section, Player player, int estimatedMoneyToGet, String item, int amount) {
		if (playerFileYaml.getInt("Money_Earned_Per_Item." + section) == -1)
			playerFileYaml.set("Money_Earned_Per_Item." + section, 0);
		playerFileYaml.set("Money_Earned_Per_Item." + section, playerFileYaml.getInt("Money_Earned_Per_Item." + section) + estimatedMoneyToGet);
		saveyaml(playerFileYaml, getFile(player.getUniqueId().toString()));

		//int rhines = amountOfItemsFoundInInv * (Integer.parseInt(item.getItemMeta().getLore().get(0).substring(9).replaceFirst(" Rhines per item.", "")));
		player.sendMessage(ChatColor.GRAY + "You've sold " + ChatColor.YELLOW + (item.replace("_", " ").toLowerCase() + ChatColor.GRAY + " for " + ChatColor.GOLD + estimatedMoneyToGet + " Rhines" + ChatColor.GRAY + "."));
	    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 1f);
		
	    addToBal(player.getUniqueId().toString(), estimatedMoneyToGet);
		
		this.getServer().getConsoleSender().sendMessage(player.getName() + " sold " + amount + " " + item.replace("_", " ").toLowerCase());
	}

	private String findSection(YamlConfiguration yaml, String item) {
		String result = "";
		for (String section : yaml.getConfigurationSection("Price_Per_Item").getKeys(false)) 
		{
			if (yaml.getInt("Price_Per_Item." + section + "." + item) != 0)
			{
				result = section + "." + item;
				break;
			}
		}
		return result;
	}


	public File getFile(String UUIDcode) {
		YamlConfiguration playerFileYaml;
		File playerFile = new File(getDataFolder(), "PlayerData" + File.separatorChar + UUIDcode + ".yml");
		if(!playerFile.exists()){
			try {
				playerFile.createNewFile();
				playerFileYaml = (YamlConfiguration) this.getConfig();

				playerFileYaml.createSection("Money_Earned_Per_Item");
				for (String section : playerFileYaml.getConfigurationSection("Price_Per_Item").getKeys(false)) 
				{
					for (String item : playerFileYaml.getConfigurationSection("Price_Per_Item." + section).getKeys(false)) 
					{
						playerFileYaml.createSection("Money_Earned_Per_Item." + section + "." + item);
						playerFileYaml.set("Money_Earned_Per_Item." + section + "." + item, -1);
					}
					
				}
				saveyaml(playerFileYaml, playerFile);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
        } 
		return playerFile;
	}
	
	private int wowoBigBrain(int startPrice, int x) {
		return (int) Math.ceil( (1+startPrice)*Math.exp( -x*Math.log(1+startPrice)/500000 )-1 );
	}
	
	
	//ok
	public int getPrice(YamlConfiguration yaml, String section) 
	{
		return yaml.getInt("Price_Per_Item." + section);
	}

	//ok
	private int getCurrentAmountMoneyEarned(YamlConfiguration playerFileYaml, String section) 
	{
		if (playerFileYaml.getInt("Money_Earned_Per_Item." + section) == -1)
			return 0;
		else
			return playerFileYaml.getInt("Money_Earned_Per_Item." + section);
	}
	
	
	
	private void addToBal(String playeruuid, int rhines) 
	{
		cashyaml.set("PlayerData." + playeruuid, cashyaml.getInt("PlayerData." + playeruuid) + rhines);
	    saveyaml(cashyaml, moneyfile);
	}
	
	private void startCooldown(Player player) {
		onCooldown.add(player.getName());
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				onCooldown.remove(player.getName());
			}
		}, 10L);
		
	}
	
	/* ---------------=o=O=o=--------------- */
	

	@EventHandler(ignoreCancelled = true)
	public void onPlayerClickShop(PlayerInteractEvent event) 
	{
		if(event.getHand() != EquipmentSlot.HAND)
			return;
		
		// Checks if player clicked sign.
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) 
		{
			if (allSigns.contains(event.getClickedBlock().getType()))
			{
				// Checks if sign is a shop.
				loadFiles();
				
				Location loc = event.getClickedBlock().getLocation();
				File clickedShopFile = new File(shopfiles, loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ() + ".yml");
				
				if (!clickedShopFile.exists())
				{
					unloadFiles();
					return;
				}
				
				YamlConfiguration clickedShopYaml = YamlConfiguration.loadConfiguration(clickedShopFile);
				
				// Create signshop click object.
				event.setCancelled(true);
				BlockState state = event.getClickedBlock().getState();
                if (state instanceof Sign) 
                {
                    final Sign clickedSign = (Sign) state;
                    CreateSignShopClick(clickedShopFile, clickedShopYaml, event.getPlayer().getName(), event.getPlayer().isSneaking(), clickedSign);
                }

				unloadFiles();
			}
		}
	}
	
	
	
	
	/*
	 * Set containing all signShopClick instances.
	 */
	private Set<SignShop> signShopClicks = new HashSet<SignShop>();
	
	/*
	 * Creates an signShopClick instance for a player.
	 * If there is already an instance with this player, terminate it.
	 */
	private void CreateSignShopClick(@Nonnull File file, @Nonnull YamlConfiguration yaml, @Nonnull String playername, boolean isShifting, Sign sign) {	
		if (!signShopClicks.isEmpty()) tryClearSignShopClicks(playername);
			
		createCorrectType(file, yaml, playername, isShifting, sign);
	}
	
	private void tryClearSignShopClicks(String playername) {
		for (SignShop click : signShopClicks)
		{
			if (click.getPlayer() == null || click.getPlayer().getName() == playername) 
			{
				click.setAllVarsNull();
				click = null;
			}
		}
		Set<SignShop> temp = new HashSet<SignShop>();
		for (SignShop click : signShopClicks)
		{
			if (click != null)
				temp.add(click);
		}
		signShopClicks = temp;
	}
	

	
	

	private void createCorrectType(File file, YamlConfiguration yaml, String playername, boolean isShifting, Sign sign) {
		if (sign.getLine(0).contains(ChatColor.BOLD + "-[Buy]-"))
		{
			GenericBuy temp = new GenericBuy(this, file, yaml, playername, isShifting, sign);
			signShopClicks.add(temp);
			terminateMe(temp);
		}
		
		else if (sign.getLine(0).contains(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=[Buy]="))
		{
			AdminBuy temp = new AdminBuy(this, file, yaml, playername, isShifting, sign);
			signShopClicks.add(temp);
			terminateMe(temp);
		}
		
		else if (sign.getLine(0).contains(ChatColor.BOLD + "-[Sell]-"))
		{
			GenericSell temp = new GenericSell(this, file, yaml, playername, isShifting, sign);
			signShopClicks.add(temp);
			terminateMe(temp);
		}
		
		else if (sign.getLine(0).contains(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "=[Sell]="))
		{
			AdminSell temp = new AdminSell(this, file, yaml, playername, isShifting, sign);
			signShopClicks.add(temp);
			terminateMe(temp);
		}
		
		else
		{
			Bukkit.getPlayer(playername).sendMessage(ChatColor.GRAY + "Something went wrong! code:001");
		}
	}
	
	
	
	

	/*
	 * Terminates SignShop object by removing it from signShopClicks set and setting it to null.
	 */
	public void terminateMe(@Nonnull SignShop object) {
		if (signShopClicks.contains(object)) 
		{
			object.setAllVarsNull();
			signShopClicks.remove(object);
			object = null;
		}
		else
		{
			this.getServer().getConsoleSender().sendMessage("Tried terminating signshop object but wasn't in set!");
		}
	}
	
	
	//--------------------------------------------------------------------------------//
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.WANDERING_TRADER) 
		{
			if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Server Shop")) {
				event.getPlayer().openInventory(shop.getInv(event.getPlayer()));
				event.getPlayer().sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.WHITE + "You can also do " + ChatColor.YELLOW + "/shop" + ChatColor.WHITE + " to open this menu.");
			}
		}
		
		else if (event.getRightClicked().getType() == EntityType.VILLAGER)
		{
			if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Herbert") && isPirate(event.getPlayer(), ChatColor.RED + "Only pirates can sell stuff here!")) {
				blackMarketShizzle();
				Inventory inv = createBMInv(11, ((Chest) Bukkit.getWorld("world").getBlockAt(new Location( Bukkit.getWorld("world"), -619, 45, 3862)).getState()));
				event.getPlayer().openInventory(inv);
				
			}
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "George") && isPirate(event.getPlayer(), ChatColor.RED + "Only pirates can sell stuff here!")) {
				blackMarketShizzle();
				Inventory inv = createBMInv(13, ((Chest) Bukkit.getWorld("world").getBlockAt(new Location( Bukkit.getWorld("world"), -637, 44, 3865)).getState()));
				event.getPlayer().openInventory(inv);
			}
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Otto") && isPirate(event.getPlayer(), ChatColor.RED + "Only pirates can sell stuff here!")) {
				blackMarketShizzle();
				Inventory inv = createBMInv(15, ((Chest) Bukkit.getWorld("world").getBlockAt(new Location( Bukkit.getWorld("world"), -634, 44, 3868)).getState()));
				event.getPlayer().openInventory(inv);
			}
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Ben") && isActualPirate(event.getPlayer(), ChatColor.RED + "Only Pirates can buy a grappling hook here!")) {
				tryBuyGH(event.getPlayer(), 50000);
			}
			
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Edward") && isActualPirate(event.getPlayer(), ChatColor.RED + "Edward only does business with Pirates he can trust.")) {
				blackMarketShizzle();
				event.getPlayer().openInventory(createBMenchantInv());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private Inventory createBMenchantInv() {
		MainItemShop base = new MainItemShop(this);
		Inventory temp = base.getInv();
		
		Inventory result = Bukkit.createInventory(null, 27, "Black Market: Enchants");
		result.setContents(temp.getContents());
		
		result.setItem(11, null);
		result.setItem(13, null);
		result.setItem(15, null);
		
		ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		pane.setItemMeta(meta);
		
		int[] paneSlots = {0, 8, 18, 26};
		for (int i : paneSlots) result.setItem(i, pane);
		
		ItemStack endrod = new ItemStack(Material.END_ROD, 1);
		meta = endrod.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "-");
		endrod.setItemMeta(meta);
		endrod.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
		
		int[] endrodSlots = {4, 12, 14, 22};
		for (int i : endrodSlots) result.setItem(i, endrod);
		
		loadFiles();
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
		String chosenEnchant = blackmarketYaml.getString("ChosenEnchant");
		EnchantmentStorageMeta Emeta = (EnchantmentStorageMeta) book.getItemMeta();
		Emeta.addStoredEnchant(Enchantment.getByName(chosenEnchant), Enchantment.getByName(chosenEnchant).getMaxLevel() + 1, true);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.DARK_GRAY + "-------------------");
		lore.add(ChatColor.GOLD + "Click to buy me for " + ChatColor.YELLOW + blackmarketYaml.getInt("Price_Per_Item.Enchants." + chosenEnchant) + " Rhines" + ChatColor.GOLD + ".");
		Emeta.setLore(lore);
		book.setItemMeta(Emeta);
		unloadFiles();
		
		result.setItem(13, book);
		
		return result;
	}

	private void tryBuyGH(Player player, int price) 
	{
		loadFiles();
		if (!enoughMoney(player.getUniqueId().toString(), price)) 
		{
			player.sendMessage(ChatColor.RED + "You don't have enough money to pay for that!");
			return;
		}
		player.sendMessage(ChatColor.GRAY + "You've bought a grappling hook for 50,000 Rhines.");
		Bukkit.dispatchCommand(getServer().getConsoleSender(), "gh give " + player.getName() + " 75");
		payMoneyForPurchase(player.getUniqueId().toString(), price);
		unloadFiles();
	}
	
	private boolean isPirate(Player player, String message) {
		if (!getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".ActiveBranch").contains("Pirate")) {
			player.sendMessage(message);
			return false;
		}
		return true;
	}
	
	private boolean isActualPirate(Player player, String message) {
		if (!getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".PirateRanks").contains("pirate")) {
			player.sendMessage(message);
			return false;
		}
		return true;
	}
	
	private void blackMarketShizzle() {
		loadFiles();
		if (checkIfNewNeeded()) // Check if needs to be updated
		{ 
			Map<String, Chest> sections = new HashMap<String, Chest>();
			sections.put("Crops", ((Chest) Bukkit.getWorld("world").getBlockAt(new Location( Bukkit.getWorld("world"), -619, 45, 3862)).getState()));
			sections.put("Mining", ((Chest) Bukkit.getWorld("world").getBlockAt(new Location( Bukkit.getWorld("world"), -637, 44, 3865)).getState()));
			sections.put("MobDrops", ((Chest) Bukkit.getWorld("world").getBlockAt(new Location( Bukkit.getWorld("world"), -634, 44, 3868)).getState()));
			
			for (String section : sections.keySet())
			{
				List<Material> allItemsInSection = getSellableItemsFrom(section);
				Set<Material> chosenItems = new HashSet<Material>();
				for (int i = 0; i < 5; i++) {
					int temp = getRandomNumberInRange(0, allItemsInSection.size()-1);
					chosenItems.add(allItemsInSection.get(temp));
					allItemsInSection.remove(temp);
					
				}
				
				sections.get(section).getInventory().clear();
				
				for (Material mat : chosenItems) {
					ItemStack item = createBMitem(mat, section);
					sections.get(section).getInventory().addItem(item);
				}
			}
			
			Set<String> enchants = blackmarketYaml.getConfigurationSection("Price_Per_Item.Enchants").getKeys(false);
			String chosenEnchant = getRandomObjectFromSet(enchants);
			blackmarketYaml.set("ChosenEnchant", chosenEnchant);
			saveyaml(blackmarketYaml, blackmarketFile);

			
			File bigFile = new File(getDataFolder(), "PlayerData");
			for (File playerFile : bigFile.listFiles()) {
				YamlConfiguration playerFileYaml = YamlConfiguration.loadConfiguration(playerFile);
				playerFileYaml.set("MoneyEarnedInBM", 0);
				playerFileYaml.set("Bought_Enchant", false);
				saveyaml(playerFileYaml, playerFile);
			}
		}
		unloadFiles();
	}
	
	private String getRandomObjectFromSet(Set<String> enchants) {
		int index = getRandomNumberInRange(0, enchants.size());
		Iterator<String> iter = enchants.iterator();
		for (int i = 0; i < index; i++) {
		    iter.next();
		}
		return iter.next();
	}
	
	private boolean checkIfNewNeeded() {
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_WEEK) != blackmarketYaml.getInt("Day")) {
			// Update Day
			blackmarketYaml.set("Day", cal.get(Calendar.DAY_OF_WEEK));
			saveyaml(blackmarketYaml, blackmarketFile);
			return true;
		}
		else
			return false;
	}
	
	private Inventory createBMInv(int type, Chest chest) {
		MainItemShop base = new MainItemShop(this);
		Inventory temp = base.getInv();
		Inventory result;
		if (type == 11) result = Bukkit.createInventory(null, 27, "Black Market: Crops");
		else if (type == 13) result = Bukkit.createInventory(null, 27, "Black Market: Mining");
		else result = Bukkit.createInventory(null, 27, "Black Market: Drops");
		
		result.setContents(temp.getContents());
		
		result.setItem(4, result.getItem(type));
		
		int slot = 11;
		for (ItemStack items : chest.getInventory()) {
			if (items == null) continue;
			result.setItem(slot++, items);
		}
		
		return result;
	}
	
	private ItemStack createBMitem(Material mat, String section) {
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add("");
		lore.add(ChatColor.GRAY + "Amount of items you will sell: all.");
		

		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		
		String rname = "";
		String name = item.getType().toString().toLowerCase();
		String[] name2 = name.split("_");
		for (int i = 0; i < name2.length; i++) {
			name2[i] = name2[i].replaceFirst(name2[i].substring(0, 1), name2[i].substring(0, 1).toUpperCase()) + " ";
			rname = rname + name2[i];
		}
		meta.setDisplayName(ChatColor.DARK_AQUA + rname);
		
		int price = blackmarketYaml.getInt("Price_Per_Item." + section + "." + mat.toString());
		
		lore.set(0, ChatColor.YELLOW + "Value: " + price + " Rhines per item.");
		lore.set(1, ChatColor.GOLD + "" + price*64 + " Rhines for a stack");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private List<Material> getSellableItemsFrom(String section) {
		List<Material> list = new ArrayList<Material>();
		for (Object key : blackmarketYaml.getConfigurationSection("Price_Per_Item." + section).getKeys(false)) {
			try {
				list.add(Material.getMaterial(key.toString()));
			}
			catch (Exception ignored) {};
		}
		return list;
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
}
