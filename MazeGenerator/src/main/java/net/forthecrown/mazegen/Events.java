package net.forthecrown.mazegen;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.HashSet;
import java.util.Set;

public class Events implements Listener {

    private final Set<Player> cooldown = new HashSet<>();

    private final Main main;
    public Events(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onEventEnter(PlayerInteractAtEntityEvent event){
        if(!event.getRightClicked().getWorld().getName().equals("world_void")) return;
        if(!event.getRightClicked().isInvulnerable()) return;
        if(event.getRightClicked().getCustomName() == null) return;

        if(event.getRightClicked().getCustomName().contains(ChatColor.GOLD + "Harold") && event.getRightClicked().getType() == EntityType.VILLAGER){
            event.setCancelled(true);
            if(cooldown.contains(event.getPlayer())) return;

            if(main.inMaze != null){
                event.getPlayer().sendMessage(ChatColor.GRAY + "There is already a player in the maze!");
                return;
            }

            cooldown.add(event.getPlayer());
            main.enterEvent(event.getPlayer());
            doCooldownThings(event.getPlayer());

        } else if (event.getRightClicked().getCustomName().contains("Exit the maze!") && event.getRightClicked().getType() == EntityType.ARMOR_STAND){
            event.setCancelled(true);
            if(cooldown.contains(event.getPlayer())) return;
            if(main.inMaze == null || main.inMaze != event.getPlayer()) return;
            main.finishEvent(event.getPlayer());

            event.getRightClicked().remove();
        }
    }

    private void doCooldownThings(Player player){
        cooldown.add(player);
        Bukkit.getScheduler().runTaskLater(main, () -> cooldown.remove(player), 20);
    }
}
