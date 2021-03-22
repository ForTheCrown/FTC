package net.forthecrown.cosmetics;

import net.kyori.adventure.text.Component;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

public class PlayerRider implements Listener {

    public final Player rider;
    public final Player ridden;
    private final Cosmetics main;
    private final Slime seat;

    public PlayerRider(Player rider, Player ridden, Cosmetics main) {
        this.rider = rider;
        this.ridden = ridden;
        this.main = main;

        this.seat = getSlimeSeat(rider.getLocation());

        main.getServer().getPluginManager().registerEvents(this, main);
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

        new BukkitRunnable(){
            public void run(){
                preventBadLocation();
            }
        }.runTaskLater(main, 5);

        HandlerList.unregisterAll(this);
        Cosmetics.getRider().riders.remove(this);
    }

    //hehehe bald
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
