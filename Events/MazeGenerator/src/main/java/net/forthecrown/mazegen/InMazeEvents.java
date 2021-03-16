package net.forthecrown.mazegen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

public class InMazeEvents implements Listener {

    private final Main main;
    public InMazeEvents(Main main){
        this.main = main;
    }

    private final Set<Player> cooldown = new HashSet<>();

    //------------------------------------------
    // FOR THE LOVE OF FUCK, MAKE SURE THESE
    // EVENTS GET UNREGISTERED WHEN THERE'S NO ONE IN THE MAZE
    //------------------------------------------

    @EventHandler
    public void onPlayerWalk(PlayerMoveEvent event){
        if(main.inMaze != event.getPlayer()) return;

        Block block = event.getFrom().subtract(0, 1, 0).getBlock();
        if(block.getType() == Material.ANDESITE) block.setType(Material.DIORITE);
    }

    public int i = 3;
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(cooldown.contains(event.getPlayer())) return;
        if(event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        if(player.getInventory().getItemInMainHand().getType() != Material.FIREWORK_STAR) return;
        if(!player.getInventory().getItemInMainHand().hasItemMeta()) return;
        if(!main.getDestroyableSurfaces().contains(event.getClickedBlock().getType())) return;

        Location placeLoc = new Location(Bukkit.getWorld("world_void"), event.getClickedBlock().getX(), Main.POS_1.getBlockY(), event.getClickedBlock().getZ());

        placeLoc.getBlock().setType(Material.AIR);
        placeLoc.add(0, 1, 0).getBlock().setType(Material.AIR);
        placeLoc.add(0, 1, 0).getBlock().setType(Material.AIR);
        placeLoc.createExplosion(1, false, false);
        placeLoc.add(0, 1, 0).getBlock().setType(Material.AIR);

        if(i > 3) i = 3;

        i--;
        if(i < 1){
            player.getInventory().removeItemAnySlot(main.getBomber());
            return;
        }

        player.sendMessage(ChatColor.GRAY + "Bomb was used! " + ChatColor.YELLOW + i + " uses" + ChatColor.GRAY + " remaining.");

        cooldown.add(player);
        Bukkit.getScheduler().runTaskLater(main, () -> cooldown.remove(player), 20);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(main.inMaze == null || main.inMaze != event.getPlayer()) return;
        main.endEvent(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(main.inMaze == null || main.inMaze != event.getEntity()) return;
        main.endEvent(event.getEntity());
    }
}
