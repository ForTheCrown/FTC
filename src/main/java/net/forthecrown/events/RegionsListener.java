package net.forthecrown.events;

import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.ListIterator;

public class RegionsListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        cancelPlayerAction(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        cancelPlayerAction(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFromToEvent event) {
        cancelPlayerAction(null, event.getToBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        piston(event, event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        piston(event, event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        explode(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        explode(event.blockList());
    }

    private void explode(List<Block> blocks) {
        ListIterator<Block> iterator = blocks.listIterator();

        while (iterator.hasNext()) {
            Block b = iterator.next();
            PopulationRegion region = manager.get(RegionPos.of(b.getX(), b.getZ()));

            if(region.getPoleBounds().contains(b)) {
                iterator.remove();
            }
        }
    }

    private void piston(BlockPistonEvent event, List<Block> blocks) {
        for (Block b: blocks) {
            PopulationRegion region = manager.get(RegionPos.of(b.getX(), b.getZ()));

            if(region.getPoleBounds().contains(b)) {
                event.setCancelled(true);
            }
        }
    }

    private void cancelPlayerAction(Player player, Block block, Cancellable event) {
        if (player != null) {
            if (player.hasPermission(Permissions.REGIONS_ADMIN)) {
                return;
            }

            if (!player.getWorld().equals(Worlds.overworld())) {
                return;
            }
        }

        PopulationRegion region = manager.get(RegionPos.of(block.getX(), block.getZ()));

        if (region.getPoleBounds().contains(block)) {
            event.setCancelled(true);
        }
    }

    private final RegionManager manager = RegionManager.get();
}