package net.forthecrown.poshd;

import net.forthecrown.crown.EventTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    public static final Location HAZELGUARD = new Location(Bukkit.getWorld("world"), 200, 74, 200);

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        eventLogic(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        eventLogic(event.getPlayer());
    }

    void eventLogic(Player player) {
        if(player.getWorld().getName().contains("posh_event") && !player.hasPermission("ftc.admin")) {
            player.teleport(HAZELGUARD);
        }

        EventTimer timer = Main.TIMERS.remove(player.getUniqueId());
        if(timer == null) return;
        if(!timer.wasStopped()) timer.stop();

        EventUtil.leave(player, HAZELGUARD);
    }
}
