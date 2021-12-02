package net.forthecrown.crownevents;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface InEventListener extends Listener {

    default void register(Plugin plugin){
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    default void unregister(){
        HandlerList.unregisterAll(this);
    }

}
