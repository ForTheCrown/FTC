package net.forthecrown.mayevent.events;

import net.forthecrown.core.crownevents.InEventListener;
import net.forthecrown.mayevent.ArenaEntry;
import net.forthecrown.mayevent.MayMain;
import net.forthecrown.mayevent.arena.EventArena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ArenaListener extends InEventListener<ArenaEntry> {

    public EventArena arena;

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!arena.box.contains(event.getEntity())) return;

        arena.currentMobAmount--;
        arena.updateBossbar();

        arena.checkBossbar();
        arena.checkHighlighting();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!entry.player().equals(event.getEntity())) return;

        event.setCancelled(true);
        MayMain.event.complete(entry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!entry.player().equals(event.getPlayer())) return;

        MayMain.event.end(entry);
    }
}
