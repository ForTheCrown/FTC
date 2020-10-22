package ftc.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static Main plugin; //idk, I need this to call the config file in other classes

    @Override
    public void onEnable(){
        plugin = this;

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new StaffSpeakEvent(), this);

        getCommand("staffchat").setExecutor(new StaffChatCommand());
        getCommand("staffchattoggle").setExecutor(new StaffChatToggleCommand());
        getCommand("staffchat").setTabCompleter(new SCTabCompleter());
    }

}
