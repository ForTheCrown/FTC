package net.forthecrown.waypoints.listeners;

import java.util.Collection;
import java.util.Collections;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.Waypoints;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WaypointDestroyListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockBreak(BlockBreakEvent event) {
    checkDestroy(Collections.singleton(event.getBlock()));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onEntityExplode(EntityExplodeEvent event) {
    checkDestroy(event.blockList());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockExplode(BlockExplodeEvent event) {
    checkDestroy(event.blockList());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    checkDestroy(event.getBlocks());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    checkDestroy(event.getBlocks());
  }

  private static void checkDestroy(Collection<Block> blocks) {
    if (blocks.isEmpty()) {
      return;
    }

    var waypoints = WaypointManager.getInstance()
        .getChunkMap()
        .getOverlapping(
            blocks.iterator().next().getWorld(),
            Bounds3i.of(blocks)
        );

    if (waypoints.isEmpty()) {
      return;
    }

    Tasks.runLater(() -> {
      waypoints.forEach(Waypoints::removeIfPossible);
    }, 1);
  }
}