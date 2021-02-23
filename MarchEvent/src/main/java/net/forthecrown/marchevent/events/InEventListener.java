package net.forthecrown.marchevent.events;

import net.forthecrown.marchevent.EventMain;
import net.forthecrown.marchevent.PvPEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class InEventListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!PvPEvent.inEvent.contains(event.getPlayer())) return;

        EventMain.getEvent().removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!PvPEvent.inEvent.contains(event.getEntity())) return;
        if(event.getEntity().getKiller() == null) return;
        if(!PvPEvent.inEvent.contains(event.getEntity().getKiller())) return;

        event.setKeepLevel(true);
        event.setKeepInventory(true);

        Objective obj = EventMain.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("crown");
        Score killerScr = obj.getScore(event.getEntity().getKiller().getName());
        if(!killerScr.isScoreSet()) killerScr.setScore(3);
        else killerScr.setScore(killerScr.getScore()+2);

        event.getEntity().sendMessage("You a bitch");
        event.getEntity().getKiller().sendMessage("You not a bitch");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!PvPEvent.inEvent.contains(event.getPlayer())) return;
    }
}
