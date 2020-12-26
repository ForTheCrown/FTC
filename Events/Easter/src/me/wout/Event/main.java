package me.wout.Event;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;


public class main extends JavaPlugin implements Listener {
	
	public File playerfile;
	public YamlConfiguration playeryaml;
	public List<String> players = new ArrayList<String>();
	public List<ItemStack> itemsToGet = new ArrayList<ItemStack>();
	
	@SuppressWarnings("unchecked")
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Check datafolder.
		File dir = getDataFolder();
		if (!dir.exists())
			if (!dir.mkdir())
				System.out.println("Could not create directory for plugin: " + getDescription().getName());
				
		// Check raidsyaml
		playerfile = new File(getDataFolder(), "playersThatCompletedEvent.yml");
		if(!playerfile.exists()){
			try {
				playerfile.createNewFile();
				playeryaml = YamlConfiguration.loadConfiguration(playerfile);
				playeryaml.createSection("Players");
				playeryaml.set("Players", players);
				saveyaml(playeryaml, playerfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	playeryaml = YamlConfiguration.loadConfiguration(playerfile);
        }
		
		getServer().getPluginManager().registerEvents(this, this);
		
		players = (List<String>) playeryaml.getList("Players");
		
		itemsToGet.add(new ItemStack(Material.LIME_DYE, 1));
		itemsToGet.add(new ItemStack(Material.GREEN_DYE, 1));
		itemsToGet.add(new ItemStack(Material.YELLOW_DYE, 1));
		itemsToGet.add(new ItemStack(Material.ORANGE_DYE, 1));
		itemsToGet.add(new ItemStack(Material.RED_DYE, 1));
		itemsToGet.add(new ItemStack(Material.PINK_DYE, 1));
		itemsToGet.add(new ItemStack(Material.PURPLE_DYE, 1));
		itemsToGet.add(new ItemStack(Material.BLACK_DYE, 1));
		itemsToGet.add(new ItemStack(Material.LIGHT_BLUE_DYE, 1));
		itemsToGet.add(new ItemStack(Material.BLUE_DYE, 1));
	}
	
	

	private void saveyaml(YamlConfiguration yaml, File file) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void onDisable() {
		playeryaml.set("Players", players);
		saveyaml(playeryaml, playerfile);
	}
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND))
			return;
		Player player = (Player) event.getPlayer();
		if (event.getRightClicked().getType() == EntityType.VILLAGER) {
			if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Harold")) {
				if (players.contains(player.getName())) {
					player.sendMessage(ChatColor.RED + "You've already helped Harold.");
					return;
				}
				
				if (checkIfInvContainsAllItems(event.getPlayer().getInventory()) == true) {
					takeItems(event.getPlayer().getInventory());
					player.sendMessage("Harold " + ChatColor.GRAY + ChatColor.BOLD + ">" + ChatColor.RESET + " Well done, Happy Easter!");
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
					giveReward(player);
					players.add(player.getName());
				}
				else {
					player.sendMessage(ChatColor.GOLD + "You need all these eggs in your inventory:");
					player.sendMessage(ChatColor.YELLOW + "Red, Orange, Yellow, Lime, Green");
					player.sendMessage(ChatColor.YELLOW + "Light blue, Blue, Pink, Purple, Black");
				}
			}
		}
	}
	
	
	private void giveReward(Player player) {
		int x = this.getConfig().getInt("x");
		int y = this.getConfig().getInt("y");
		int z = this.getConfig().getInt("z");
		World world = Bukkit.getWorld(this.getConfig().getString("world"));
		if (world.getBlockAt(x, y, z).getType() != Material.CHEST) {
			Bukkit.getConsoleSender().sendMessage("[Valentine] Block at " + x + ", " + y + ", " + z + " is not a chest.");
			player.sendMessage(ChatColor.RED + "Something went wrong. Contact Wout");
			return;
		}
		Chest chest = (Chest) world.getBlockAt(x, y, z).getState();
		ItemStack item = chest.getInventory().getItem(0);
		for (int i = 0; i < 36; i++) {
			if (player.getInventory().getItem(i) == null) {
				player.getInventory().addItem(item);
				return;
			}
		}
	}



	private void takeItems(PlayerInventory inv) {
		int size = 36;
		
		for (ItemStack item : itemsToGet) {
	    	for (int i = 0; i < size; i++) {
	    		ItemStack invItem = inv.getItem(i);
	    		if (inv.getItem(i) != null) {
	    			
		    		if (invItem.getType() == item.getType() && invItem.getAmount() >= item.getAmount()) {
		    			invItem.setAmount(invItem.getAmount() - item.getAmount());;
		    		}
	    		}
	    	}
	    	
		}
		return;
	}



	private boolean checkIfInvContainsAllItems(PlayerInventory inv) {
		int size = 36;
		Boolean found;
		
		for (ItemStack item : itemsToGet) {
			found = false;
			
	    	for (int i = 0; i < size; i++) {
	    		ItemStack invItem = inv.getItem(i);
	    		if (invItem != null) {
	    			
		    		if (invItem.getType() == item.getType() && invItem.getAmount() >= item.getAmount()) {
		    			if (invItem.hasItemMeta() && invItem.getItemMeta().hasLore() && invItem.getItemMeta().getLore().contains("You've found a hidden Easter egg!")) {
		    				found = true;
		    				break;
		    			}
		    		}
	    		}
	    	}
	    	
	    	if (!found) {
	    		return false;
	    	}
		}
		return true;
	}
}