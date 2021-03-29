package net.forthecrown.easteregghunt.events;

import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.easteregghunt.EasterEvent;
import net.forthecrown.easteregghunt.EasterMain;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class EventListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        Villager villie = (Villager) event.getRightClicked();
        if(!villie.getWorld().equals(CrownUtils.WORLD_VOID)) return;
        if(villie.getCustomName() == null) return;
        if(!villie.getCustomName().contains("Harold") || !villie.isInvulnerable()) return;

        Player player = event.getPlayer();
        if(!player.getInventory().isEmpty()) throw new CrownException(player, "&7You must have an empty inventory");
        if(!EasterMain.tracker().entryAllowed(player)) throw new CrownException(player, "&7You've done too many rounds today. Come back tomorrow :)");
        if(!EasterEvent.open) throw new CrownException(player, "&7Someone is currently in the event");

        EasterMain.event.start(player);
    }
}
