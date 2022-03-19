package net.forthecrown.events;

import net.forthecrown.core.battlepass.challenges.Challenges;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class BattlePassListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;
        if (!(event.getEntity() instanceof Animals animal)) return;
        if (animal.fromMobSpawner()) return;

        Challenges.KILL_PASSIVES.trigger(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!event.getBlock().getType().name().contains("ORE")) return;

        Challenges.MINE_ORES.trigger(event.getPlayer().getUniqueId());
    }


}
