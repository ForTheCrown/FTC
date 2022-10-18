package net.forthecrown.events.player;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.events.Events;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRidingListener implements Listener {
    private static final Map<UUID, Rider> BY_PASSENGER = new HashMap<>();
    private static final Map<UUID, Rider> BY_VEHICLE = new HashMap<>();

    @EventHandler
    public void playerRightClickPlayer(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player rider)) {
            return;
        }

        if (rider.getGameMode() == GameMode.SPECTATOR
                || rider.getWorld().equals(Worlds.voidWorld())
                || event.getHand() == EquipmentSlot.OFF_HAND
                || ItemStacks.isEmpty(rider.getInventory().getItemInMainHand())
        ) {
            return;
        }

        Player ridden = (Player) event.getRightClicked();
        User riderUser = Users.get(rider);
        User riddenUser  = Users.get(ridden);

        if (rider.isInsideVehicle()
                || !ridden.getPassengers().isEmpty()
                || !rider.getPassengers().isEmpty()
                || Cooldown.containsOrAdd(riderUser, 10)
                || !canRide(riderUser, riddenUser)
        ) {
            return;
        }

        if (!ridden.getPassengers().isEmpty()) {
            return;
        }

        //WorldGuard stuffs
        var ridingAllowed = FtcFlags.query(ridden.getLocation(), FtcFlags.RIDING_ALLOWED);

        if (StateFlag.denyToNone(ridingAllowed) == null) {
            riderUser.sendMessage(
                    Text.renderString("&c&lHey! &7You can't ride players here.")
            );
            return;
        }

        startRiding(rider, ridden);
    }

    public static void startRiding(Player rider, Player passenger) {
        Rider riderM = new Rider(rider, passenger);
        Events.register(riderM);

        addRider(riderM);
    }

    public static boolean canRide(User user, User ridden){
        if (user.getPlayer().isSneaking()) {
            return false;
        }

        if (!user.get(Properties.PLAYER_RIDING)
                || !ridden.get(Properties.PLAYER_RIDING)
        ) {
            user.sendMessage(Messages.BOTH_ALLOW_RIDING);
            return false;
        }

        Location loc = ridden.getLocation();
        var oneAbovePlayer = loc.add(0, 2, 0).getBlock();
        var twoAbovePlayer = loc.add(0, 1, 0).getBlock();

        if (oneAbovePlayer.isCollidable()
                || twoAbovePlayer.isCollidable()
        ) {
            user.sendActionBar(Messages.CANNOT_RIDE_HERE);
            return false;
        }

        return true;
    }

    public static void stopAllRiding() {
        for (var e: Lists.newArrayList(BY_VEHICLE.values())) {
            e.stopRiding();
        }
    }

    public static Rider getByPassenger(Player player) {
        return BY_PASSENGER.get(player.getUniqueId());
    }

    public static Rider getByVehicle(Player player) {
        return BY_VEHICLE.get(player.getUniqueId());
    }

    public static boolean isBeingRidden(Player player){
        return getByVehicle(player) != null;
    }

    public static boolean isRiding(Player player){
        return getByPassenger(player) != null;
    }

    public static boolean riddenOrRider(Player player){
        return isRiding(player) || isBeingRidden(player);
    }

    public static void addRider(Rider rider) {
        BY_PASSENGER.put(rider.passenger.getUniqueId(), rider);
        BY_VEHICLE.put(rider.vehicle.getUniqueId(), rider);
    }

    public static void removeRider(Rider rider) {
        BY_PASSENGER.remove(rider.passenger.getUniqueId());
        BY_VEHICLE.remove(rider.vehicle.getUniqueId());
    }

    public static void stopRiding(Player player) {
        var passenger = getByPassenger(player);
        var vehicle = getByVehicle(player);

        if (vehicle != null) {
            vehicle.stopRiding();
        }

        if (passenger != null) {
            passenger.stopRiding();
        }
    }

    public static class Rider implements Listener {
        public static final NamespacedKey SLIME_KEY = Keys.forthecrown("riding_slime");

        public final Player passenger;
        public final Player vehicle;

        private final Entity seat;

        public Rider(Player passenger, Player vehicle) {
            this.passenger = passenger;
            this.vehicle = vehicle;

            seat = getSlimeSeat(passenger.getLocation());
            startRiding();
        }

        private Entity getSlimeSeat(Location loc) {
            return loc.getWorld().spawn(loc, Slime.class, slime -> {
                slime.setInvisible(true);
                slime.setSize(0);

                slime.setAI(false);
                slime.setGravity(false);
                slime.setCustomNameVisible(false);
                slime.setInvulnerable(true);

                slime.getPersistentDataContainer().set(
                        SLIME_KEY,
                        PersistentDataType.BYTE,
                        (byte) 1
                );

                slime.customName(
                        Component.text(passenger.getName() + '_' + vehicle.getName())
                );
            });
        }

        public void startRiding() {
            seat.addPassenger(passenger);
            vehicle.addPassenger(seat);
        }

        public void stopRiding() {
            stopRiding(true);
        }

        public void stopRiding(boolean ensureSafe) {
            seat.eject();
            vehicle.eject();
            seat.remove();

            HandlerList.unregisterAll(this);

            if (ensureSafe) {
                Tasks.runLater(this::preventBadLocation, 5);
            }

            removeRider(this);
        }

        private void preventBadLocation(){
            if(vehicle.getLocation().getBlockY() <= Util.MIN_Y
                    || passenger.getLocation().getBlockY() <= Util.MIN_Y
            ) {
                vehicle.teleport(Crown.config().getServerSpawn());
                passenger.teleport(Crown.config().getServerSpawn());
            }
        }

        private void riderCheck(Entity riderE){
            if(!riderE.equals(passenger)) {
                return;
            }

            stopRiding();
        }

        private void riddenCheck(Entity riddenE) {
            if(!riddenE.equals(vehicle)) {
                return;
            }

            stopRiding();
        }

        public void checkSeat(Entity seat){
            if(!seat.equals(this.seat)) {
                return;
            }
            stopRiding();
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityDismount(EntityDismountEvent event) {
            checkSeat(event.getDismounted());
            checkSeat(event.getEntity());
            riderCheck(event.getEntity());
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerDeath(PlayerDeathEvent event) {
            riderCheck(event.getEntity());
            riddenCheck(event.getEntity());
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerQuit(PlayerQuitEvent event) {
            riderCheck(event.getPlayer());
            riddenCheck(event.getPlayer());
        }

        @EventHandler
        public void playerLaunchOtherPlayer(PlayerInteractEntityEvent event) {
            if(!event.getPlayer().equals(vehicle)) {
                return;
            }

            if(event.getPlayer().getLocation().getPitch() > (-75) && !event.getPlayer().isSneaking()) {
                return;
            }

            stopRiding();
        }
    }
}