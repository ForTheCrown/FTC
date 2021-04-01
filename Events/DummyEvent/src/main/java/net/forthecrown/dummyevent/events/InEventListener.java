package net.forthecrown.dummyevent.events;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.crownevents.entries.TimerEntry;
import net.forthecrown.dummyevent.SprintMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class InEventListener implements Listener {

    public static CrownBoundingBox FINISH_AREA = new CrownBoundingBox(Bukkit.getWorld("world_void"), -552, 113, 462, -560, 100, 478);
    public static CrownBoundingBox CHECKPOINT_AREA = new CrownBoundingBox(Bukkit.getWorld("world_void"), -640, 113, 547, -629, 103, 519);
    private boolean canEnd = false;

    private final Player player;
    public TimerEntry entry;

    public InEventListener(Player player){
        this.player = player;

        SprintMain.plugin.getServer().getPluginManager().registerEvents(this, SprintMain.plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!event.getPlayer().equals(player)) return;

        if(FINISH_AREA.contains(event.getTo()) && canEnd) SprintMain.event.complete(entry);
        if(CHECKPOINT_AREA.contains(event.getTo())) canEnd = true;
    }
}
