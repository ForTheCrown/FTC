package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.Punisher;
import net.forthecrown.utils.Locations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JailCellListener implements Listener {
    private final Punisher punisher;

    public JailCellListener() {
        punisher = Crown.getPunisher();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        JailCell cell = punisher.getCell(event.getPlayer().getUniqueId());
        if(cell == null) return;

        Player player = event.getPlayer();

        if(!cell.getCell().contains(player)) {
            player.teleport(Locations.of(cell.getWorld(), cell.getPos()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        JailCell cell = punisher.getCell(event.getPlayer().getUniqueId());
        if(cell == null) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Cannot use commands while jailed").color(NamedTextColor.RED));
    }
}