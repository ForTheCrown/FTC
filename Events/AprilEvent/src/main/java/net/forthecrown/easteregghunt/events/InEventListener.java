package net.forthecrown.easteregghunt.events;

import net.forthecrown.easteregghunt.EasterEntry;
import net.forthecrown.easteregghunt.EasterEvent;
import net.forthecrown.easteregghunt.EasterMain;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

public class InEventListener implements Listener {

    public EasterEntry entry;
    public EasterEvent event;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(!event.getPlayer().equals(entry.player())) return;
        if(!event.getRightClicked().getPersistentDataContainer().has(EasterMain.spawner.key, PersistentDataType.BYTE)) return;

        Player player = entry.player();
        entry.inc();
        event.getRightClicked().getLocation().getBlock().setType(Material.AIR);
        event.getRightClicked().remove();
        player.sendMessage("Score up");
    }

    private void checkEntity(Entity entity){
        if(!entity.equals(entry.player())) return;
        event.end(entry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        checkEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        checkEntity(event.getPlayer());
    }
}
