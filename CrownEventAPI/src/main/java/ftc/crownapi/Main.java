package ftc.crownapi;

import ftc.crownapi.commands.CrownAPICommand;
import ftc.crownapi.commands.CrownAPITabCompleter;
import ftc.crownapi.settings.CrownBooleanSettings;
import ftc.crownapi.settings.CrownSettings;
import ftc.crownapi.types.CrownEvent;
import ftc.crownapi.types.CrownEventUser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Main extends JavaPlugin {

    private final APIListener defaultListener = new APIListener();
    public static Main plugin;

    public List<Player> playersInEvent;
    public List<Player> quitPlayersList;
    public List<Player> disqualifiedPlayersList;
    public Map<Player, Integer> scoreMap;

    public CrownEvent crownMain = new CrownEvent();

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(defaultListener, this);

        getServer().getPluginCommand("crownapi").setExecutor(new CrownAPICommand());
        getServer().getPluginCommand("crownapi").setTabCompleter(new CrownAPITabCompleter());

        initializeLists();
    }

    @Override
    public void onDisable() {
        //Teleports player to Hazelguard when the plugin disables
        if(CrownSettings.getSetting(CrownBooleanSettings.REMOVE_ON_SHUTDOWN) && playersInEvent.size() > 0){
            for (Player p : playersInEvent){
                new CrownEventUser(p).teleportToHazelguard();
            }
        }
        saveLists();
    }

    public void saveLists(){
        getConfig().set("lists.quitPlayers", quitPlayersList);
        getConfig().set("lists.disqualifiedPlayers", disqualifiedPlayersList);
        saveConfig();
    }
    public void initializeLists(){
        try{
            quitPlayersList = (List<Player>) getConfig().getList("lists.quitPlayers");
        } catch (NullPointerException e){
            quitPlayersList = new ArrayList<>();
        }
        try {
            disqualifiedPlayersList = (List<Player>) getConfig().getList("lists.disqualifiedPlayers");
        } catch (NullPointerException e1){
            disqualifiedPlayersList = new ArrayList<>();
        }
    }
}
