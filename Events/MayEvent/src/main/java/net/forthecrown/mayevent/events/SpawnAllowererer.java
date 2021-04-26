package net.forthecrown.mayevent.events;

import net.forthecrown.mayevent.MayMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.List;

public class SpawnAllowererer implements Listener {

    public List<Location> locs;

    public SpawnAllowererer(List<Location> locs){
        this.locs = locs;
        Bukkit.getPluginManager().registerEvents(this, MayMain.inst);
    }

    public Location add(Location l){
        locs.add(l);
        return l;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(locs.contains(event.getLocation())) event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if(locs.contains(event.getLocation())) event.setCancelled(false);
    }
}
