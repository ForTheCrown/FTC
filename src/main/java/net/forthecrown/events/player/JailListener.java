package net.forthecrown.events.player;

import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.Punisher;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JailListener implements Listener {
    private final Punisher punisher;

    public JailListener() {
        punisher = Punishments.get();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        JailCell cell = punisher.getCell(event.getPlayer().getUniqueId());

        if (cell == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!cell.getCell().contains(player)) {
            var pos = cell.getPos();
            player.teleport(new Location(cell.getWorld(), pos.x(), pos.y(), pos.z()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        JailCell cell = punisher.getCell(event.getPlayer().getUniqueId());

        if (cell == null) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(Messages.JAIL_NO_COMMANDS);
    }
}