package net.forthecrown.mazegen;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class Events implements Listener {

    Set<Player> inMaze = new HashSet<>();

    private final Main main;
    public Events(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onEventEnter(PlayerInteractAtEntityEvent event){
        if(event.getRightClicked().getType() != EntityType.ARMOR_STAND) return;
        if(!event.getRightClicked().isInvulnerable()) return;
        if(!event.getRightClicked().getCustomName().contains(ChatColor.YELLOW + "Click me to enter the event!")) return;
        event.setCancelled(true);

        if(main.getConfig().getLocation("EntryPos") == null){
            event.getPlayer().sendMessage("There is no set entry position for the maze!");
            return;
        }
        if(inMaze.size() > 0){
            event.getPlayer().sendMessage("There is already a player in the maze!");
            return;
        }

        Location loc = main.getConfig().getLocation("EntryPost");
        Player player = event.getPlayer();
        inMaze.add(player);

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*20, 1));
    }
}
