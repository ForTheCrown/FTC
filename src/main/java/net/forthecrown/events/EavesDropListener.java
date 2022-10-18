package net.forthecrown.events;

import net.forthecrown.core.admin.EavesDropper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class EavesDropListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        var player = event.getPlayer();
        EavesDropper.reportSign(player, event.getBlock(), event.lines());
    }
}