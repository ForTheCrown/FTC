package net.forthecrown.core.events;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.user.data.UserTeleport;
import net.forthecrown.core.utils.CrownUtils;
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
        CrownCore.getJailManager().addJailListener(this);

        checkDistance();
    }

    public boolean invertedPlayerCheck(Player player){ return !player.equals(this.player); }
    public void checkDistance(){ if(player.getLocation().distance(jail) > 7.5) player.teleport(jail); }
    public void checkJailed(){
        if(System.currentTimeMillis() < record.expiresAt) return;
        CrownCore.getPunishmentManager().checkJailed(player);

        release();
    }

    public void unreg(){
        HandlerList.unregisterAll(this);
        CrownCore.getJailManager().removeJailListener(this);
    }

    public void release(){
        unreg();

        UserManager.getUser(player).createTeleport(() -> CrownUtils.LOCATION_HAZELGUARD, false, true, UserTeleport.Type.OTHER)
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
