package net.forthecrown.mayevent.events;

import net.forthecrown.mayevent.DoomEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class MayListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(event.getLocation().getWorld().equals(DoomEvent.EVENT_WORLD)) event.setCancelled(false);
    }
}
