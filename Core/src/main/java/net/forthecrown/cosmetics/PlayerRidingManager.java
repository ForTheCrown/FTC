package net.forthecrown.cosmetics;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.core.Crown;
import net.forthecrown.core.WgFlags;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;

public class PlayerRidingManager implements Listener {

    private final ObjectSet<PlayerRider> riders;

    static final NamespacedKey SEAT_KEY = new NamespacedKey(Crown.inst(), "player_seat");
    static final Location RETREAT_LOCATION = new Location(Bukkit.getWorld("world"), 200.5, 71, 1000.5);

    PlayerRidingManager(){
        this.riders = new ObjectArraySet<>();
        Bukkit.getPluginManager().registerEvents(this, Crown.inst());

        Crown.logger().info("Player Riding Manager loaded");
    }

    public ObjectSet<PlayerRider> getRiders() {
        return riders;
    }

    @EventHandler
    public void playerRightClickPlayer(PlayerInteractEntityEvent event) {
        if(event.getHand() == EquipmentSlot.OFF_HAND) return;
        if(!FtcUtils.isItemEmpty(event.getPlayer().getInventory().getItemInMainHand())) return;
        if(event.getPlayer().getWorld().equals(Worlds.VOID)) return;
        if(event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if(!(event.getRightClicked() instanceof Player)) return;

        Player rider = event.getPlayer();
        Player riddenPlayer = (Player) event.getRightClicked();
        CrownUser user = UserManager.getUser(rider);
        CrownUser ridden  = UserManager.getUser(riddenPlayer);

        if(!canRide(user, ridden)) return;
        if(Cooldown.contains(user)) return;
        Cooldown.add(user, 20);

        if(rider.isInsideVehicle() || riddenPlayer.getPassengers().size() > 0 || rider.getPassengers().size() > 0) return;
        if(!riddenPlayer.getPassengers().isEmpty()) return;

        //WorldGuard stuffs
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(rider);
        ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(wgPlayer.getLocation());
        if(!set.testState(wgPlayer, WgFlags.RIDING_ALLOWED)){
            user.sendMessage("&c&lHey! &7You can't ride players here.");
            return;
        }

        PlayerRider riderM = new PlayerRider(rider, riddenPlayer);
        Bukkit.getPluginManager().registerEvents(riderM, Crown.inst());
        addRider(riderM);
    }

    public boolean canRide(CrownUser user, CrownUser ridden){
        if(user.getPlayer().isSneaking()) return false;

        if(!user.allowsRiding() || !ridden.allowsRiding()){
            user.sendMessage(Component.translatable("user.bothAllowRiding", NamedTextColor.GRAY));
            return false;
        }

        Location loc = ridden.getLocation();
        Material oneAbovePlayer = loc.add(0, 2, 0).getBlock().getType();
        Material twoAbovePlayer = loc.add(0, 1, 0).getBlock().getType();

        if(!isAllowedBlock(oneAbovePlayer) || !isAllowedBlock(twoAbovePlayer)){
            user.sendActionBar(Component.translatable("user.cannotRideHere").color(NamedTextColor.GRAY));
            return false;
        }

        return true;
    }

    private boolean isAllowedBlock(Material material){
        if(material.isAir()) return true;
        if(material.isEmpty()) return true;
        if(!material.isSolid()) return true;

        return false;
    }

    public void stopAllRiding() {
        Set<PlayerRider> riders = getRiders();
        for (PlayerRider r: riders)
            r.stopRiding();
    }

    public PlayerRider getByRider(Player player){
        for (PlayerRider r: riders){
            if(r.rider.equals(player)) return r;
        }

        return null;
    }

    public PlayerRider getByRidden(Player player){
        for (PlayerRider r: riders){
            if(r.ridden.equals(player)) return r;
        }

        return null;
    }

    public boolean isBeingRidden(Player player){
        return getByRidden(player) != null;
    }

    public boolean isRiding(Player player){
        return getByRider(player) != null;
    }

    public boolean riddenOrRider(Player player){
        return isRiding(player) || isBeingRidden(player);
    }

    public void addRider(PlayerRider rider){
        riders.add(rider);
    }

    public void removeRider(PlayerRider rider){
        riders.remove(rider);
    }
}