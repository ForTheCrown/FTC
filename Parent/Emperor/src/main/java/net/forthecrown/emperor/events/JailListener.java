package net.forthecrown.emperor.events;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.admin.record.PunishmentRecord;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.emperor.utils.CrownUtils;
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

    private final Player player;
    private final Location jail;
    private final PunishmentRecord record;

    public JailListener(Player player, Location jail){
        this.player = player;
        this.jail = jail;

        record = CrownCore.getPunishmentManager().getEntry(player.getUniqueId()).getCurrent(PunishmentType.JAIL);

        checkDistance();
    }

    public boolean invertedPlayerCheck(Player player){ return !player.equals(this.player); }
    public void checkDistance(){ if(player.getLocation().distance(jail) > 7.5) player.teleport(jail); }
    public void checkJailed(){
        if(System.currentTimeMillis() < record.expiresAt) return;
        CrownCore.getPunishmentManager().checkJailed(player);

        HandlerList.unregisterAll(this);
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
