package ftc.randomfeatures;

import ftc.randomfeatures.commands.*;
import ftc.randomfeatures.features.GraveFeature;
import ftc.randomfeatures.features.HurtMobHealthBar;
import ftc.randomfeatures.features.SmokeBomb;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	
	public File graves;
	public YamlConfiguration gravesyaml;

	//For the HealthBar
	public Map<UUID, String> withSetNames = new HashMap<>();

	//constructors or something
	public HurtMobHealthBar healthBar = new HurtMobHealthBar();
	public SmokeBomb smokeBomb = new SmokeBomb();
	public GraveFeature graveFeature = new GraveFeature();
	//public WildPortal wildPortal = new WildPortal();

	/* Wild Portal doesn't get constructed because I thought it'd be better if we used the ancient gates console command on portal enter thingy
	   for it. So I made the wild command executable by the console, it requires an a player's name as an arg then.
	   We'll have to change like 2 wild portals on the server then tho lol */
	
	public void onEnable() {
		plugin = this;
		
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Commands
		getServer().getPluginCommand("crowntop").setExecutor(new Crowntop());
		getServer().getPluginCommand("deathtop").setExecutor(new Deathtop());

		getServer().getPluginCommand("grave").setExecutor(new Grave());
		getServer().getPluginCommand("wild").setExecutor(new wild());
		getServer().getPluginCommand("christmasgift").setExecutor(new ChristmasGift());

		//Events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		pm.registerEvents(healthBar, this);
		pm.registerEvents(smokeBomb, this);
		pm.registerEvents(graveFeature, this);
		//pm.registerEvents(wildPortal, this);
		
		// Check datafolder.
		File dir = getDataFolder();
		if (!dir.exists())
			if (!dir.mkdir())
				System.out.println("Could not create directory for plugin: " + getDescription().getName());
		
	}
	
	// -----
	
	
	public void onDisable() {
		loadFiles();
		saveyaml(gravesyaml, graves);
		unloadFiles();

		for(UUID uuid: withSetNames.keySet()){
			Bukkit.getEntity(uuid).setCustomName(withSetNames.getOrDefault(uuid, null));
		}
	}
	
	public void loadFiles() {
		graves = new File(getDataFolder(), "GravesData.yml");
		if(!graves.exists()){
			try {
				graves.createNewFile();
				gravesyaml = YamlConfiguration.loadConfiguration(graves);
				saveyaml(gravesyaml, graves);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	gravesyaml = YamlConfiguration.loadConfiguration(graves);
        }
	}
	
	public void unloadFiles() {
		saveyaml(gravesyaml, graves);
		gravesyaml = null;
		graves = null;
	}
	
	private void saveyaml(YamlConfiguration yaml, File file) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}