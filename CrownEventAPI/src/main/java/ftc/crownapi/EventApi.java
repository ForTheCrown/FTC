package ftc.crownapi;

import ftc.crownapi.commands.*;
import ftc.crownapi.config.CrownBooleanSettings;
import ftc.crownapi.types.CrownEvent;
import ftc.crownapi.types.CrownEventUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class EventApi extends JavaPlugin {

    public static EventApi plugin;

    public List<UUID> playersInEvent = new ArrayList<>();
    public List<UUID> quitPlayersList = new ArrayList<>();
    public List<UUID> disqualifiedPlayersList = new ArrayList<>();
    public Map<Player, Integer> scoreMap = new HashMap<>();
    public static List<CrownEventUser> loadedUsers = new ArrayList<>();

    private CrownEvent crownEvent;

    @Override
    public void onEnable() {
        plugin = this;
        crownEvent = new CrownEvent(getInstance());
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new APIListener(crownEvent, this), this);

        getServer().getPluginCommand("crownapi").setExecutor(new CrownAPICommand(this, crownEvent));
        getServer().getPluginCommand("crownapi").setTabCompleter(new CrownAPITabCompleter());

        getServer().getPluginCommand("disqualify").setExecutor(new DisqualifyCommand());

        getServer().getPluginCommand("kingmaker").setExecutor(new KingMakerCommand(this, crownEvent));
        getServer().getPluginCommand("kingmaker").setTabCompleter(new KingMakerTabCompleter());

        initializeLists();
    }

    @Override
    public void onDisable() {
        removePlayers();
        saveLists();
    }

    //use this to get the user! ALWAYS! Otherwise it might not be able to supply the correct things needed to construct the user class,
    //like the main class and the crownMain class. Also, it prevents multiple instances of the same  user  being created, I hope
    public static CrownEventUser getApiUser(Player base){
        for(CrownEventUser user : loadedUsers){
            if(user.getPlayer() == base) return user;
        }
        CrownEventUser crownEventUser = new CrownEventUser(base, plugin, getInstance().getCrownEvent());
        return crownEventUser;
    }

    public static String getKing(){
        return plugin.getConfig().getString("king");
    }
    public static void setKing(String king){
        plugin.getConfig().set("king", king);
    }
    public CrownEvent getCrownEvent(){
        //if(crownEvent == null) crownEvent = new CrownEvent(getInstance());
        return crownEvent;
    }
    public static void saveCrownConfig(){
        plugin.saveConfig();
    }
    public static void reloadCrownConfig(){
        plugin.reloadConfig();
    }
    public static EventApi getInstance(){
        return plugin;
    }

    // The following 2 methods are for ease of use or something. So you don't have to copy paste this exact same code in other plugins

    // Creates an item.
    // You  can just do EventApi.makeItem(material, 69, true, "bruh", "bruh" "bruh");... or something
    public static ItemStack makeItem(Material material, int amount, boolean hideFlags, String name, String... loreStrings){
        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        if (name != null) meta.setDisplayName(name);
        if (loreStrings != null) {
            List<String> lore = new ArrayList<>();
            Collections.addAll(lore, loreStrings);
            meta.setLore(lore);
        }
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        result.setItemMeta(meta);
        return result;
    }

    // Same as above, just do EventApi.getRandomNumberInRange(4, 6);
    // Gives a random int within a specified range. Min and max are possible results.
    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            return 0;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    //Teleports players, that are in the event, to Hazelguard when the plugin disables
    private void removePlayers(){
        if(getCrownEvent().getSetting(CrownBooleanSettings.REMOVE_ON_SHUTDOWN) && playersInEvent.size() >= 1){
            for (UUID id : playersInEvent){
                Player p = Bukkit.getPlayer(id);
                getApiUser(p).teleportToHazelguard();
            }
        }
    }

    //list methods
    //lists need to be converted from UUID lists to String lists, otherwise they can't be saved in the config
    private void saveLists(){
        List<String> stringDisList = new ArrayList<>();
        List<String> stringQuitList = new ArrayList<>();

        for(UUID uuid : disqualifiedPlayersList){
            stringDisList.add(uuid.toString());
        }
        for(UUID uuid : quitPlayersList){
            stringQuitList.add(uuid.toString());
        }

        getConfig().set("lists.quitPlayers", stringQuitList);
        getConfig().set("lists.disqualifiedPlayers", stringDisList);
        saveConfig();
    }
    private void initializeLists(){
        List<String> stringDisList = new ArrayList<>();
        List<String> stringQuitList = new ArrayList<>();

        try{
            stringQuitList = getConfig().getStringList("lists.quitPlayers");
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        try {
            stringDisList = getConfig().getStringList("lists.disqualifiedPlayers");
        } catch (NullPointerException e1){
            e1.printStackTrace();
        }

        for(String id : stringDisList){
            disqualifiedPlayersList.add(UUID.fromString(id));
        }for(String id : stringQuitList){
            quitPlayersList.add(UUID.fromString(id));
        }
    }
}
