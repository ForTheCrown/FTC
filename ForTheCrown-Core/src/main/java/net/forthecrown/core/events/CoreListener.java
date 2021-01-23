package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class CoreListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        FtcCore.getUser(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        UUID id = event.getPlayer().getUniqueId();
        FtcCore.getUser(id).unload();
    }
}
