package ftc.bigcrown;

import ftc.bigcrown.commands.BigBootyEventCommand;
import ftc.bigcrown.commands.BigBootyTabCompleter;
import ftc.bigcrown.events.Events;

import java.util.*;

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
	public List<Integer> locsInUse = new ArrayList<>();
	
	public int delay = 3; //in minutes
	public boolean runLoop = false;

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
	}


	// Present spawning and loop
	public void loop() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (!runLoop) return;
				else {
					locsInUse.clear();
					spawnPresent();
					loop();
				}
			}
		}, /*delay*20*60*/ 60);
	}
	
	public void stopLoop(){
		this.runLoop = false;
	}

	// Gets the list of Present Locations from the config.
	@SuppressWarnings("unchecked")
	public List<Location> getLocationList() {
		return (List<Location>) getConfig().getList("PresentList");
	}
	
	// Checks if a location is suited for spawning
	private boolean canSpawnPresent(int id) {
		if (this.locsInUse.contains(id) || getLocationList().get(id).getBlock().getType() != Material.AIR) return false;
		else return true;
	}
	

	public void spawnPresent(){
		List<Location> locList = getLocationList();
		
		// Pick random number, if it's already in use,
		// pick a new random number (added saveGuard just in case of endless loop).
		int saveGuard = 300;
		int locId = getRandomNumberInRange(0, locList.size() - 1);
		
		while ((!canSpawnPresent(locId)) && saveGuard > 0) {
			locId = getRandomNumberInRange(0, locList.size() - 1);
			saveGuard--;
		}
		if (saveGuard <= 0) { Bukkit.getConsoleSender().sendMessage("Endless loop :("); return; }
		else locsInUse.add(locId);

		
		// Actual spawning part
		int rotation = (int) (Math.random() * 15);
		Location spawnLoc = getLocationList().get(locId);
		Location loc = new Location(spawnLoc.getWorld(), spawnLoc.getBlockX()+.5, spawnLoc.getBlockY(), spawnLoc.getBlockZ()+.5, (float) (rotation*22.5), 0);
		Slime slime = (Slime) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
		slime.setInvisible(true);
		slime.setSilent(true);
		slime.setPersistent(true);
		slime.setSize(1);
		slime.setInvulnerable(true);
		slime.setAI(false);
		slime.setCustomNameVisible(false);
		slime.setCustomName(ChatColor.GOLD + "Present");

		String headCommand = "minecraft:setblock " +  loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " minecraft:player_head[rotation=" + rotation + "]" + presentNBTs.get((int) (Math.random()*presentNBTs.size()-1)) + " replace";
		getServer().dispatchCommand(Bukkit.getConsoleSender(), headCommand);

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				slime.remove();
				spawnLoc.getBlock().setType(Material.AIR);
			}
		}, /*30*20*60*/ 585L); // It should get removed after 30 mins
	}
	

	
	// Gives a random int within a specified range. Min and max are possible results.
	public int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	// Creates an item.
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
