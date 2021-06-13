package net.forthecrown.core.events;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.Grave;
import net.forthecrown.core.user.UserManager;
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

    private static final Component message = Component.text("[FTC] You have items in your grave. ")
            .color(NamedTextColor.GRAY)
            .append(Component.text()
                    .append(Component.text("/grave"))
                    .append(Component.text(" or ").color(NamedTextColor.GRAY))
                    .append(Component.text("[click here]"))

                    .color(NamedTextColor.YELLOW)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to claim your items")))
                    .clickEvent(ClickEvent.runCommand("/grave"))
            )
            .append(Component.text(" to claim them."));

    private void sendGraveMessage(Player p){
        CrownUser user = UserManager.getUser(p);
        if(user.getGrave().isEmpty()) return;
        user.sendMessage(message);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getKeepInventory()) return;
        CrownUser user = UserManager.getUser(event.getEntity());
        user.setLastLocation(event.getEntity().getLocation());

        Grave grave = user.getGrave();
        for (ItemStack i: event.getEntity().getInventory()){
            if(CrownItems.isCrownItem(i)){
                grave.addItem(i);
                event.getDrops().remove(i);
            }
        }
    }
}
