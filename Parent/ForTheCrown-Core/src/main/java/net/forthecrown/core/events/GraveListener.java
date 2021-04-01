package net.forthecrown.core.events;

import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.Grave;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class GraveListener implements Listener {

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
        Component component = Component.text("[FTC] You have items in your ")
                .color(NamedTextColor.GRAY)
                .append(Component.text("/grave")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(HoverEvent.showText(Component.text("Click to claim your items")))
                        .clickEvent(ClickEvent.runCommand("/grave"))
                );
        user.sendMessage(component);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getKeepInventory()) return;
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
