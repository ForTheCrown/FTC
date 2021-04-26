package net.forthecrown.core.crownevents;

import net.forthecrown.core.crownevents.entries.EventEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class InEventListener<T extends EventEntry<T>> implements Listener {

    protected T entry;

    public final void setEntry(T entry){
        if(this.entry != null) throw new IllegalStateException("Cannot redefine entry");
        this.entry = entry;
    }

    public final void register(Plugin plugin){
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public final void unregister(){
        HandlerList.unregisterAll(this);
    }
}
