package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.Grave;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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

    public static final Component MESSAGE = Crown.prefix()
            .color(NamedTextColor.GRAY)
            .append(Component.translatable("grave.haveItems",
                    Component.translatable("grave.option", NamedTextColor.AQUA)
                            .hoverEvent(Component.translatable("grave.option.hover"))
                            .clickEvent(ClickEvent.runCommand("/grave"))
            ));

    private void sendGraveMessage(Player p){
        CrownUser user = UserManager.getUser(p);
        if(user.getGrave().isEmpty()) return;
        user.sendMessage(MESSAGE);
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
            if(FtcItems.isCrownItem(i)){
                grave.addItem(i);
                event.getDrops().remove(i);
            }
        }
    }
}
