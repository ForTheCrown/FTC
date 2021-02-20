package net.forthecrown.marchevent;

import org.bukkit.plugin.java.JavaPlugin;

public final class MarchEvent extends JavaPlugin {

    private static MarchEvent plugin;

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public MarchEvent getPlugin(){
        return plugin;
    }
}
