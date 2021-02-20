package net.forthecrown.core.crownevents;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public abstract class CrownEvent implements Listener {

    public static final Set<Player> inEvent = new HashSet<>();
    private final Plugin plugin;

    protected final Location startLocation;
    protected final Location exitLocation;

    protected CrownEvent(Plugin plugin, Location startLocation, Location exitLocation) {
        this.plugin = plugin;
        this.startLocation = startLocation;
        this.exitLocation = exitLocation;
    }

    public void onEventStart(Player player){
        inEvent.add(player);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onEventEnd(Player player, EventEndCause cause){
        inEvent.remove(player);
        HandlerList.unregisterAll(this);
        player.teleport(exitLocation);

        switch (cause){
            case DEATH:
                player.sendMessage(ChatColor.GRAY + "You died mid event!");
                return;

            case TIMER_EXPIRE:
            case TIMER_DOWN_EXPIRE:
                player.sendMessage(ChatColor.GRAY + "You took too long! lol");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(inEvent.contains(event.getPlayer())) onEventEnd(event.getPlayer(), EventEndCause.QUIT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(inEvent.contains(event.getEntity())) onEventEnd(event.getEntity(), EventEndCause.DEATH);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
    }
}
