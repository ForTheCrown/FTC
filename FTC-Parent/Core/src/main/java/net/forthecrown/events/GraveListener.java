package net.forthecrown.events;

import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.Grave;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

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
        Location loc = user.getLocation();

        user.setLastLocation(loc);

        // Tell the Player where they died, but ignore world_void deaths.
        String diedAt = "died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ".";
        if (!loc.getWorld().getName().equalsIgnoreCase("world_void"))
            user.sendMessage(ChatColor.GRAY + "[FTC] You " + diedAt);

        Announcer.log(Level.INFO, "! " + user.getName() + " " + diedAt);

        Grave grave = user.getGrave();
        for (ItemStack i: event.getEntity().getInventory()){
            if(CrownItems.isCrownItem(i)){
                grave.addItem(i);
                event.getDrops().remove(i);
            }
        }
    }
}
