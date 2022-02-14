package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.PunishmentRecord;
import net.forthecrown.core.admin.PunishmentType;
import net.forthecrown.cosmetics.PlayerRidingManager;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserTeleport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JailListener implements Listener {

    public final Player player;
    public final Location jail;
    public final PunishmentRecord record;

    public JailListener(Player player, Location jail){
        this.player = player;
        this.jail = jail;

        record = Crown.getPunishments().getEntry(player.getUniqueId()).getCurrent(PunishmentType.JAIL);
        Crown.getJailManager().addListener(this);

        player.teleport(jail);
    }

    public boolean isNotPlayer(Player player){ return !player.equals(this.player); }
    public void checkDistance(){
        if(!checkJailed()) return;
        Location pLoc = player.getLocation();

        if(!pLoc.getWorld().equals(jail.getWorld()) || player.getLocation().distance(jail) > 7.5) {
            player.teleport(jail);
        }
    }
    public boolean checkJailed(){
        if(Crown.getPunishments().checkJailed(player)) return true;

        release();
        return false;
    }

    public void unreg(){
        HandlerList.unregisterAll(this);
        Crown.getJailManager().removeListener(this);
    }

    public void release(){
        unreg();

        UserManager.getUser(player).createTeleport(() -> PlayerRidingManager.HAZELGUARD, false, true, UserTeleport.Type.OTHER)
                .start(false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(isNotPlayer(event.getPlayer())) return;

        player.sendMessage(Component.text("Cannot use commands while jailed").color(NamedTextColor.RED));
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(isNotPlayer(event.getPlayer())) return;

        checkDistance();
    }
}