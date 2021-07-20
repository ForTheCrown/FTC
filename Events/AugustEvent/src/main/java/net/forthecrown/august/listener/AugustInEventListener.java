package net.forthecrown.august.listener;

import net.forthecrown.august.AugustEntry;
import net.forthecrown.august.A_Main;
import net.forthecrown.august.EventUtil;
import net.forthecrown.crownevents.InEventListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class AugustInEventListener implements InEventListener, Listener {

    private final AugustEntry entry;
    private final Player player;
    private final Entity pinata;

    public AugustInEventListener(AugustEntry entry, Player player) {
        this.entry = entry;
        this.player = player;

        this.pinata = EventUtil.findPinata();
    }

    public boolean checkPlayer(Entity entity) {
        return player.equals(entity);
    }

    public boolean checkPinata(Entity entity) {
        return pinata.equals(entity);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if(!checkPlayer(event.getPlayer())) return;
        A_Main.event.end(entry);
    }

    
}
