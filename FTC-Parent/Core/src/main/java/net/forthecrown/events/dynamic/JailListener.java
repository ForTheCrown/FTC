package net.forthecrown.events.dynamic;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.utils.FtcUtils;
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

        record = CrownCore.getPunishmentManager().getEntry(player.getUniqueId()).getCurrent(PunishmentType.JAIL);
        CrownCore.getJailManager().addListener(this);

        checkDistance();
    }

    public boolean invertedPlayerCheck(Player player){ return !player.equals(this.player); }
    public void checkDistance(){
        if(!checkJailed()) return;
        if(player.getLocation().distance(jail) > 7.5) player.teleport(jail);
    }
    public boolean checkJailed(){
        if(CrownCore.getPunishmentManager().checkJailed(player)) return true;

        release();
        return false;
    }

    public void unreg(){
        HandlerList.unregisterAll(this);
        CrownCore.getJailManager().removeListener(this);
    }

    public void release(){
        unreg();

        UserManager.getUser(player).createTeleport(() -> FtcUtils.LOCATION_HAZELGUARD, false, true, UserTeleport.Type.OTHER)
                .start(false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(invertedPlayerCheck(event.getPlayer())) return;

        player.sendMessage(Component.text("Cannot use commands while jailed").color(NamedTextColor.RED));
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(invertedPlayerCheck(event.getPlayer())) return;

        checkDistance();
    }
}
