package me.wout.netherevent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wout.netherevent.commands.ItemList;
import me.wout.netherevent.commands.Leave;

public class main extends JavaPlugin implements Listener {


	public File playerDataFile;
	
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
		
		
		
		new ItemList(this);
		//new Leave(this);
		
		playerDataFile = new File(getDataFolder(), File.separator + "PlayerData");
		if(!playerDataFile.exists()) {
			playerDataFile.mkdirs();
        }
	}
	
	public void saveyaml(YamlConfiguration yaml, File file) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public File getFile(String UUIDcode) {
		YamlConfiguration playerFileYaml;
		File playerFile = new File(getDataFolder(), "PlayerData" + File.separatorChar + UUIDcode + ".yml");
		if(!playerFile.exists()){
			try {
				playerFile.createNewFile();
				playerFileYaml = (YamlConfiguration) this.getConfig();

				playerFileYaml.createSection("Items_Found");
				playerFileYaml.createSection("Items_Found.FirstInv");
				playerFileYaml.createSection("Items_Found.SecondInv");
				playerFileYaml.createSection("Items_Found.ThirdInv");
				playerFileYaml.createSection("Items_Found.Extra");
				playerFileYaml.set("Items_Found.FirstInv", new ArrayList<>());
				playerFileYaml.set("Items_Found.SecondInv", new ArrayList<>());
				playerFileYaml.set("Items_Found.ThirdInv", new ArrayList<>());
				playerFileYaml.set("Items_Found.Extra", new ArrayList<>());
				
				playerFileYaml.createSection("Leave_Location");
				playerFileYaml.createSection("Leave_Location.x");
				playerFileYaml.createSection("Leave_Location.y");
				playerFileYaml.createSection("Leave_Location.z");
				playerFileYaml.set("Leave_Location.x", 1);
				playerFileYaml.set("Leave_Location.y", 1);
				playerFileYaml.set("Leave_Location.z", 1);
				saveyaml(playerFileYaml, playerFile);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
        } 
		return playerFile;
	}
	
	
	// --------------------------------------- //
	
	
	// Portal to nether event world
	@EventHandler
	public void portallingIn(EntityPortalEnterEvent event)
	{
		// TODO CHANGE LOC
		Location loc = new Location(Bukkit.getWorld("nether_event"), -14, 230, -17);
		try {
			if (loc.distance(event.getEntity().getLocation()) > 3) return;
		} catch (Exception ignored) {
			return;
		}
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player) event.getEntity();
		if (failedPortalPlayers.contains(player)) return;
		if (!invClear(player))
		{
			player.sendMessage(ChatColor.GRAY + "Your inventory has to be empty to enter this portal.");
			return;
		}
		
		if(player != null) {
			int x = 0;
			int y = 75;
			int z = 0;
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getFile(player.getUniqueId().toString()));
			if (yaml.getInt("Leave_Location.x") == 1 && yaml.getInt("Leave_Location.y") == 1 && yaml.getInt("Leave_Location.z") == 1)
			{
				// x
				if (Math.random() < 0.5)
					x = getRandomNumberInRange(100, 2300);
				else 
					x = getRandomNumberInRange(-2300, -100);
				
				// z
				if (Math.random() < 0.5)
					z = getRandomNumberInRange(100, 2300);
				else 
					z = getRandomNumberInRange(-2300, -100);
				
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 1));
			}
			else {
				x = yaml.getInt("Leave_Location.x");
				y = yaml.getInt("Leave_Location.y");
				z = yaml.getInt("Leave_Location.z");
			}
			
			player.teleport(new Location(player.getWorld(), x, y, z));
			player.setBedSpawnLocation(new Location(player.getWorld(), x, y, z), true);
			
			player.sendMessage(ChatColor.GRAY + "You've been teleported. To get back, you can do " + ChatColor.YELLOW + "/leave" + ChatColor.GRAY + ", it will save your location.");
		} 
	}
	
	private Set<Player> failedPortalPlayers = new HashSet<Player>();
	
	@EventHandler
	public void actuallyPortalling(PlayerPortalEvent event) {
		if (event.getFrom().getWorld().getName().contains("nether_event"))
		{
			event.setCancelled(true);
		}
	}
	
	public boolean invClear(Player player) {
		PlayerInventory playerInv = player.getInventory();
		for (ItemStack item : playerInv)
		{
			if (item != null) 
			{
				failedPortalPlayers.add(player);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			        @Override
			        public void run() {
			        	failedPortalPlayers.remove(player);
			        }
			    }, 120L);
				return false;
			}
		}
		return true;
	}

	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	// Disable enderchests
	@EventHandler
	public void clickEchest(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENDER_CHEST)
		{
			if (event.getPlayer().getWorld().getName().contains("nether_event"))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.GRAY + "You can't do that here.");
			}
		}
	}
	
	
	// Harold tp nether event
	private Set<Player> teleportingPlayers = new HashSet<Player>();
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND)) return;
		
		Player player = (Player) event.getPlayer();
		
		if (event.getRightClicked().getType() != EntityType.VILLAGER) return;
		
		if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Harold")) {
			if (teleportingPlayers.contains(player)) return;
			teleportingPlayers.add(player);
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 4));
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		        @Override
		        public void run() {
		        	player.teleport(new Location(Bukkit.getWorld(getConfig().getString("ReturnLocation.World")), getConfig().getDouble("ReturnLocation.x"), getConfig().getDouble("ReturnLocation.y"), getConfig().getDouble("ReturnLocation.z"), -90, 0));
		        	player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1f);
		        	teleportingPlayers.remove(player);
		        }
		    }, 120L);
		}
	}
	
	
}
