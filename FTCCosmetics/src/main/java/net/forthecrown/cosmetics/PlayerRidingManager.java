package net.forthecrown.cosmetics;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

public class PlayerRidingManager implements Listener {

    private final Cosmetics main;
    final Set<PlayerRider> riders;

    static final Location RETREAT_LOCATION = new Location(Bukkit.getWorld("world"), 200.5, 71, 1000.5);

    PlayerRidingManager(Cosmetics main){
        this.main = main;

        this.riders = new HashSet<>();
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    public void beginRiding(Player rider, Player ridden){
        riders.add(new PlayerRider(rider, ridden, main));
    }

    public Set<PlayerRider> getRiders() {
        return riders;
    }

    @EventHandler
    public void playerRightClickPlayer(PlayerInteractEntityEvent event) {
        if(event.getHand() == EquipmentSlot.OFF_HAND) return;
        if(event.getPlayer().getWorld().getName().equalsIgnoreCase("world_void")) return;
        if(!(event.getRightClicked() instanceof Player)) return;

        Player rider = event.getPlayer();
        Player riddenPlayer = (Player) event.getRightClicked();
        CrownUser user = FtcCore.getUser(rider);
        CrownUser ridden  = FtcCore.getUser(riddenPlayer);

        if(!user.allowsRidingPlayers() || !ridden.allowsRidingPlayers()){
            user.sendMessage("&7You both have to allow riding players");
            return;
        }
        if(Cooldown.contains(user)) return;
        Cooldown.add(user, 10);

        if(rider.isInsideVehicle() || riddenPlayer.getPassengers().size() > 0 || rider.getPassengers().size() > 0) return;
        if(!riddenPlayer.getPassengers().isEmpty()) return;

        //WorldGuard stuffs
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(rider);
        ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(wgPlayer.getLocation());
        if(!set.testState(wgPlayer, Cosmetics.PLAYER_RIDING_ALLOWED)){
            user.sendMessage("&c&lHey! &7You can't ride players here.");
            return;
        }

        beginRiding(rider, riddenPlayer);
    }

    private boolean isSlimeSeat(Entity entity){
        if(!(entity instanceof Slime)) return false;
        return entity.isInvulnerable() && !entity.isCustomNameVisible();
    }

    /*@EventHandler
    public void playerDismountFromPlayer(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player) {
            Entity mount = event.getDismounted();
            if (isSlimySeat(mount)) {
                // Stop riding stuff
                mount.leaveVehicle();

                // Stop carrying stuff
                mount.eject();
                mount.remove();
            }
        }
    }

    @EventHandler
    public void playerCarryingPlayerLogout(PlayerQuitEvent event) {
        // Player leaves with something riding him:
        event.getPlayer().eject();
        Location loc = event.getPlayer().getLocation();
        for (Entity nearbyEntity : loc.getWorld().getNearbyEntities(loc, 0.1, 2, 0.1)) {
            if (isSlimySeat(nearbyEntity)) {
                nearbyEntity.eject();
                nearbyEntity.remove();
            }
        }
        // Player leaves while riding something
        if (event.getPlayer().isInsideVehicle()) {
            if (isSlimySeat(event.getPlayer().getVehicle())) {
                event.getPlayer().getVehicle().remove();
                event.getPlayer().leaveVehicle();
            }
        }
    }

    @EventHandler
    public void playerCarryingPlayerDies(PlayerDeathEvent event) {
        // Player leaves with something riding him:
        event.getEntity().eject();
        Location loc = event.getEntity().getLocation();
        for (Entity nearbyEntity : loc.getWorld().getNearbyEntities(loc, 0.1, 2, 0.1)) {
            if (isSlimySeat(nearbyEntity)) {
                nearbyEntity.eject();
                nearbyEntity.remove();
            }
        }
        // Player leaves while riding something
        if (event.getEntity().isInsideVehicle()) {
            if (isSlimySeat(event.getEntity().getVehicle())) {
                event.getEntity().getVehicle().remove();
                event.getEntity().leaveVehicle();
            }
        }
    }

    public boolean isSlimySeat(Entity entity) {
        return (entity.getType() == EntityType.SLIME
                && entity.getCustomName() != null
                && entity.getCustomName().contains(ChatColor.GREEN + "slimy"));
    }

    @EventHandler
    public void playerLaunchOtherPlayer(PlayerInteractEntityEvent event) {
        if (event.getHand().equals(EquipmentSlot.HAND)) {
            if ((!event.getPlayer().getPassengers().isEmpty()) && event.getPlayer().isSneaking() && event.getPlayer().getLocation().getPitch() <= (-75)) {
                //Set<Entity> playerPassengers = new HashSet<>();
                for (Entity passenger : event.getPlayer().getPassengers()) {
                    if (isSlimySeat(passenger)) {
                        passenger.leaveVehicle();
                        passenger.eject();
                        passenger.remove();
                    }
                }
                event.getPlayer().eject();
            }
        }
    }*/

}
