package net.forthecrown.cosmetics;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.utils.FtcUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

public class PlayerRider implements Listener {
    public static final NamespacedKey SLIME_KEY = Keys.key("ftc", "riding_slime");

    public final Player rider;
    public final Player ridden;
    private final Entity seat;

    public PlayerRider(Player rider, Player ridden) {
        this.rider = rider;
        this.ridden = ridden;

        seat = getSlimeSeat(rider.getLocation());
        startRiding();
    }

    private Entity getSlimeSeat(Location loc){
        ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();

        Slime slime = EntityType.SLIME.create(level);
        assert slime != null;

        net.minecraft.world.entity.Entity nms = slime;
        Mob mob = slime;

        nms.moveTo(loc.getX(), loc.getY(), loc.getZ());

        //Invisible
        nms.persistentInvisibility = true;
        nms.setSharedFlag(5, true);

        nms.setInvulnerable(true);
        nms.setNoGravity(true);
        nms.setCustomName(new TextComponent(rider.getName() + '_' + ridden.getName()));
        nms.setCustomNameVisible(false);

        slime.setSize(0, false);
        mob.setNoAi(true);

        level.addFreshEntity(slime, CreatureSpawnEvent.SpawnReason.CUSTOM);

        Entity entity = nms.getBukkitEntity();
        entity.getPersistentDataContainer().set(SLIME_KEY, PersistentDataType.BYTE, (byte) 1);

        return entity;
    }

    public void startRiding(){
        seat.addPassenger(rider);
        ridden.addPassenger(seat);
    }

    public void stopRiding() { stopRiding(true); }

    public void stopRiding(boolean ensureSafe) {
        seat.eject();
        ridden.eject();
        seat.remove();

        HandlerList.unregisterAll(this);
        if (ensureSafe) Bukkit.getScheduler().runTaskLater(Crown.inst(), this::preventBadLocation, 5);
        Cosmetics.getRideManager().removeRider(this);
    }

    private void preventBadLocation(){
        if(ridden.getLocation().getBlockY() <= FtcUtils.MIN_Y || rider.getLocation().getBlockY() <= FtcUtils.MIN_Y){
            ridden.teleport(FtcUtils.findHazelLocation());
            rider.teleport(FtcUtils.findHazelLocation());
        }
    }

    private void riderCheck(Entity riderE){
        if(!riderE.equals(rider)) return;

        stopRiding();
    }

    private void riddenCheck(Entity riddenE) {
        if(!riddenE.equals(ridden)) return;

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
        if(!event.getPlayer().equals(ridden)) return;
        if(event.getPlayer().getLocation().getPitch() > (-75) && !event.getPlayer().isSneaking()) return;

        stopRiding();
    }
}
