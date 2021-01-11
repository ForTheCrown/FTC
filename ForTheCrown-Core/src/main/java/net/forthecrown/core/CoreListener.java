package net.forthecrown.core;

import net.forthecrown.core.files.FtcUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class CoreListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        FtcCore.getUserData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        UUID id = event.getPlayer().getUniqueId();
        for(FtcUser data : FtcUser.loadedData){
            if(id == data.getBase()) data.unload();
        }
    }
}
