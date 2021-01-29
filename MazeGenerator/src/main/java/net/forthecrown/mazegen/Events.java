package net.forthecrown.mazegen;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;

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
        if(event.getRightClicked().getCustomName().contains(ChatColor.YELLOW + "Click me to enter the event!")){
            event.setCancelled(true);

            if(main.getConfig().getLocation("EntryPos") == null){
                event.getPlayer().sendMessage("There is no set entry position for the maze!");
                return;
            }
            if(inMaze.size() > 0){
                event.getPlayer().sendMessage("There is already a player in the maze!");
                return;
            }

            main.enterEvent(event.getPlayer());
        } else if (event.getRightClicked().getCustomName().contains("Exit the maze!")){
            event.setCancelled(true);

            int gems = 0;
            for (ItemStack stack: event.getPlayer().getInventory()){
                if(stack == null) continue;
                if(stack.getType() != Material.DIAMOND && !stack.hasItemMeta()) continue;

                gems++;
                event.getPlayer().getInventory().removeItem(stack);
            }

            if(gems > 0){
                event.getPlayer().sendMessage("You earned " + (gems*10) + " points from collected gems!");
                Score score = main.getServer().getScoreboardManager().getMainScoreboard().getObjective("crown").getScore(event.getPlayer().getName());
                score.setScore(score.getScore() + (gems*10));
            }


        }
    }
}
