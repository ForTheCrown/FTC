package net.forthecrown.events;

import lombok.RequiredArgsConstructor;
import net.forthecrown.useables.TriggerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public class TriggerListener implements Listener {
    private final TriggerManager manager;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // We only care about block changes, since
        // triggers are based on blocks
        if (!event.hasChangedPosition()) {
            return;
        }

        manager.run(event.getPlayer(), event.getFrom(), event.getTo());
    }
}