package net.forthecrown.core.events;

import net.forthecrown.core.CrownItems;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.Grave;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class GraveEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        sendGraveMessage(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendGraveMessage(event.getPlayer());
    }

    private void sendGraveMessage(Player p){
        CrownUser user = FtcCore.getUser(p);
        if(user.getGrave().isEmpty()) return;
        user.sendMessage("&7[FTC] You have royal items in your &e/grave&7.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        CrownUser user = FtcCore.getUser(event.getEntity());
        Grave grave = user.getGrave();
        for (ItemStack i: event.getEntity().getInventory()){
            if(CrownItems.isCrownItem(i)){
                grave.addItem(i);
                event.getDrops().remove(i);
            }
        }
    }
}
