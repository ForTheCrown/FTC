package ftc.bigcrown;

import ftc.bigcrown.commands.BigBootyEventCommand;
import ftc.bigcrown.commands.BigBootyTabCompleter;
import ftc.bigcrown.events.Events;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Main extends JavaPlugin {
	
	public static Main plugin;
	public List<String> presentNBTs = Arrays.asList(
			"{SkullOwner:{Id:[I;70626779,-413019213,1666094168,2007321829],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGRjNWUzYWZjYTZhNTg3NzM1MGQ0ZTEzNWExYjFmNmZkZmM3ZDlhNGNmYzkzNjE4YmU1NjMzNjkxMzJlMCJ9fX0=\"}]}}}",
			"{SkullOwner:{Id:[I;-593323462,1993558393,-2002637408,884576786],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0=\"}]}}}",
			"{SkullOwner:{Id:[I;-169773461,-1848163627,-1659930413,528659302],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdlNTVmY2M4MDlhMmFjMTg2MWRhMmE2N2Y3ZjMxYmQ3MjM3ODg3ZDE2MmVjYTFlZGE1MjZhNzUxMmE2NDkxMCJ9fX0=\"}]}}}",
			"{SkullOwner:{Id:[I;1712760208,-484031443,-2012826861,-56511019],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM2Mjc0YzIyZDcyNmZjMTIwY2UyNTczNjAzMGNjOGFmMjM4YjQ0YmNiZjU2NjU1MjA3OTUzYzQxNDQyMmYifX19\"}]}}}",
			"{SkullOwner:{Id:[I;2112825111,-414429584,-2029219499,-1043660854],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQ5N2Y0ZjQ0ZTc5NmY3OWNhNDMwOTdmYWE3YjRmZTkxYzQ0NWM3NmU1YzI2YTVhZDc5NGY1ZTQ3OTgzNyJ9fX0=\"}]}}}",
			"{SkullOwner:{Id:[I;-1365545979,1889357537,-1100829119,-1307254765],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM2YWY2YWQxOGUxM2YyNWE1Yjc0NDcyNTJiNWU5YWI4MTgwYzA1ZGU1OTg1ZmJhZjdiNGZjNGUxZDI0MTY2In19fQ==\"}]}}}"
	);

	public List<Integer> lastLocIDs = new ArrayList<>();
	public int delay = 30; //in minutes
	private Timer timer = new Timer();

	public void onEnable() {
		plugin = this;
		
		// Config
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Events
		getServer().getPluginManager().registerEvents(new Events(), this);
		
		// Commands
		getServer().getPluginCommand("BigBootyEvent").setExecutor(new BigBootyEventCommand());
		getServer().getPluginCommand("BigBootyEvent").setTabCompleter(new BigBootyTabCompleter());
		
		// start repeating function to spawn presents
	}


	//present spawning and loop
	public void startLoop(){
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				lastLocIDs.clear();
				for(int i = 0; i < 10 ; i++){
					presentSpawn();
				}
			}
		}, 0, delay*20*60);
	}
	public void stopLoop(){
		timer.cancel();
		timer.purge();
	}

	public void presentSpawn(){

		//location finding
		List<Location> locList = (List<Location>) getConfig().getList("PresentList");
		int locId = (int) (Math.random() * (locList.size() -1));

		if(lastLocIDs.contains(locId)){ //if a present is already in a location, find new location
			presentSpawn();
			return;
		}

		Location spawnLoc = locList.get(locId);
		if(spawnLoc.getBlock().getType() != Material.AIR){
			presentSpawn();
			return;
		}
		lastLocIDs.add(locId);

		//actual spawning part

		int rotation = (int) (Math.random() * 15);
		Location loc = new Location(spawnLoc.getWorld(), spawnLoc.getBlockX()+.5, spawnLoc.getBlockY(), spawnLoc.getBlockZ()+.5, (float) (rotation*22.5), 0);
		Slime slime = (Slime) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
		slime.setSize(1);
		slime.setInvulnerable(true);
		slime.setAI(false);
		slime.setCustomNameVisible(false);
		slime.setCustomName(ChatColor.GOLD + "Present");

		String headCommand = "minecraft:setblock " +  loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " minecraft:player_head[rotation=" + rotation + "]" + presentNBTs.get((int) (Math.random()*presentNBTs.size()-1)) + " replace";
		getServer().dispatchCommand(Bukkit.getConsoleSender(), headCommand);

		new BukkitRunnable() {
			@Override
			public void run() {
				slime.remove();
				spawnLoc.getBlock().setType(Material.AIR);
			}
		}.runTaskLater(this, delay*20*60); //it should get removed after 30 mins
	}

	public ItemStack makeItem(Material material, int amount, String name, String... loreStrings) {
		ItemStack result = new ItemStack(material, amount);
		ItemMeta meta = result.getItemMeta();

		if (name != null) meta.setDisplayName(name);
		if (loreStrings != null) {
			List<String> lore = new ArrayList<>();
			for (String string : loreStrings)
				lore.add(string);
			meta.setLore(lore);
		}
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

		result.setItemMeta(meta);
		return result;
	}
}
