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
import java.util.*;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	
	public File graves;
	public YamlConfiguration gravesyaml;

	//valentines day stuffs

	public static Set<UUID> valentinesPlayer = new HashSet<>();
	/*public static final List<ItemStack> VALENTINE_ITEMS = Arrays.asList(
			new ItemStack(Material.RED_BED),
			new ItemStack(Material.PUMPKIN_PIE, 32),
			new ItemStack(Material.ROSE_BUSH, 5),
			new ItemStack(Material.LANTERN, 2),
			new ItemStack(Material.CARROT, 10),
			new ItemStack(Material.BAKED_POTATO, 16)
			);
			*/

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
		//pm.registerEvents(new HaroldValentinesEvent(), this);
		//pm.registerEvents(wildPortal, this);

		Set<UUID> temp = new HashSet<>();
		for (String s : getConfig().getStringList("ValentinesPlayer")){
			try {
				temp.add(UUID.fromString(s));
			} catch (Exception ignored) {}
		}
		valentinesPlayer = temp;
		if(valentinesPlayer.size() > 0) pm.registerEvents(new TempEvent(), this);
	}
	
	// -----
	
	
	public void onDisable() {
		loadFiles();
		saveyaml(gravesyaml, graves);
		unloadFiles();

		for(UUID uuid: withSetNames.keySet()){
			Bukkit.getEntity(uuid).setCustomName(withSetNames.getOrDefault(uuid, null));
		}

		List<String> temp = new ArrayList<>();
		for (UUID id: valentinesPlayer){
			temp.add(id.toString());
		}
		getConfig().set("ValentinesPlayer", temp);
		saveConfig();
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