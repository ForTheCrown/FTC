package net.forthecrown.marchevent.events;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.marchevent.EventMain;
import net.forthecrown.marchevent.PvPEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class InEventListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!PvPEvent.inEvent.contains(event.getPlayer())) return;

        EventMain.getEvent().removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!PvPEvent.inEvent.contains(event.getEntity())) return;
        if(event.getEntity().getKiller() == null) return;
        if(!PvPEvent.inEvent.contains(event.getEntity().getKiller())) return;
        event.setCancelled(true);

        Player player = event.getEntity();
        EventMain.getEvent().clearItemAndEffects(player.getName());

        if(!areOnSameTeams(player, player.getKiller())) {
            Objective obj = EventMain.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("crown");
            Score killerScr = obj.getScore(player.getKiller().getName());
            if (!killerScr.isScoreSet()) killerScr.setScore(2);
            else killerScr.setScore(killerScr.getScore() + 2);

            player.sendMessage(CrownUtils.translateHexCodes("&7You were killed by &e" + player.getKiller().getName()));
            player.getKiller().sendMessage(CrownUtils.translateHexCodes("&7You killed &e" + player.getName() + "&7 and earned &e2 points"));
        }

        EventMain.getEvent().removePlayer(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                event.getEntity().teleport(PvPEvent.EXIT_LOCATION);
            }
        }.runTaskLater(EventMain.getInstance(), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player) && !PvPEvent.inEvent.contains((Player) event.getDamager())) return;
        if(!(event.getEntity() instanceof Player) && !PvPEvent.inEvent.contains((Player) event.getEntity())) return;

        Player p1 = (Player) event.getDamager();
        Player p2 = (Player) event.getEntity();
        if(areOnSameTeams(p1, p2)) event.setCancelled(true);
    }

    private boolean areOnSameTeams(Player p1, Player p2){
        if(PvPEvent.YELLOW_TEAM.contains(p1) && PvPEvent.YELLOW_TEAM.contains(p2)) return true;
        return PvPEvent.BLUE_TEAM.contains(p1) && PvPEvent.BLUE_TEAM.contains(p2);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!PvPEvent.inEvent.contains(event.getPlayer())) return;
        EventMain.getEvent().checkCentralBlocks();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!PvPEvent.inEvent.contains(event.getPlayer())) return;
        event.setCancelled(true);

        ItemStack toAdd = new ItemStack(Material.CYAN_WOOL);
        if(PvPEvent.YELLOW_TEAM.contains(event.getPlayer())) toAdd = new ItemStack(Material.YELLOW_WOOL);
        if(event.getBlock().getType() == toAdd.getType()) event.getPlayer().getInventory().addItem(toAdd);

        event.getBlock().setType(Material.AIR);
        EventMain.getEvent().checkCentralBlocks();
    }
}
