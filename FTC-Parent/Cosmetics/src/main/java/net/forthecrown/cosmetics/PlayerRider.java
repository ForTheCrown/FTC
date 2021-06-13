package net.forthecrown.cosmetics;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class PlayerRider implements Listener {

    public final Player rider;
    public final Player ridden;
    private final Slime seat;

    public PlayerRider(Player rider, Player ridden) {
        this.rider = rider;
        this.ridden = ridden;

        this.seat = getSlimeSeat(rider.getLocation());
        startRiding();
    }

    private Slime getSlimeSeat(Location loc){
        Slime s = (Slime) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
        s.setSize(1);
        s.setInvisible(true);
        s.setInvulnerable(true);
        s.setAI(false);
        s.customName(Component.text(rider.getName() + "_" + ridden.getName()));
        s.setCustomNameVisible(false);
        return s;
    }

    public void startRiding(){
        seat.addPassenger(rider);
        ridden.addPassenger(seat);
    }

    public void stopRiding() {
        seat.eject();
        ridden.eject();
        seat.remove();

        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().runTaskLater(Cosmetics.plugin, this::preventBadLocation, 5);
        Cosmetics.getRider().riders.remove(this);
    }

    private void preventBadLocation(){
        if(ridden.getLocation().getBlockY() <= 0 || rider.getLocation().getBlockY() <= 0){
            ridden.teleport(PlayerRidingManager.RETREAT_LOCATION);
            rider.teleport(PlayerRidingManager.RETREAT_LOCATION);
        }
    }

    private void playerCheck(Entity riderE){
        if(!riderE.equals(rider)) return;

        stopRiding();
    }

    public void checkSeat(Entity seat){
        if(!seat.equals(this.seat)) return;
        stopRiding();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        checkSeat(event.getDismounted());
        checkSeat(event.getEntity());
        playerCheck(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        playerCheck(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerCheck(event.getPlayer());
    }

    @EventHandler
    public void playerLaunchOtherPlayer(PlayerInteractEntityEvent event) {
        if(!event.getPlayer().equals(ridden)) return;
        if(event.getPlayer().getLocation().getPitch() > (-75) && !event.getPlayer().isSneaking()) return;

        stopRiding();
    }
}
