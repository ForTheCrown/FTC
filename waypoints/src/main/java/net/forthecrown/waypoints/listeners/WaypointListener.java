package net.forthecrown.waypoints.listeners;

import java.util.List;
import java.util.Set;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.Waypoints;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.spongepowered.math.vector.Vector3i;

class WaypointListener implements Listener {
  /* ----------------------------- REGULAR BLOCK EVENTS ------------------------------ */

  @EventHandler(ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    cancel(event.getBlock(), event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    cancel(event.getBlock(), event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockFromTo(BlockFromToEvent event) {
    cancel(event.getToBlock(), event);
  }

  private void cancel(Block block, Cancellable cancellable) {
    Vector3i pos = Vectors.from(block);

    World world = block.getWorld();
    Set<Waypoint> waypoints = Waypoints.getInvulnerable(pos, world);

    if (waypoints.isEmpty()) {
      return;
    }

    cancellable.setCancelled(true);
  }

  /* ----------------------------- EXPLOSIONS ------------------------------ */

  @EventHandler(ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent event) {
    explode(event.blockList(), event.getBlock().getWorld());
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    explode(event.blockList(), event.getLocation().getWorld());
  }

  private void explode(List<Block> blocks, World world) {
    Bounds3i bounds = Bounds3i.of(blocks);

    if (bounds == Bounds3i.EMPTY) {
      return;
    }

    var waypoints = Waypoints.getInvulnerable(bounds, world);

    if (waypoints.isEmpty()) {
      return;
    }

    var it = blocks.iterator();

    while (it.hasNext()) {
      var next = it.next();

      for (var w : waypoints) {
        if (w.getBounds().contains(next)) {
          it.remove();
          break;
        }
      }
    }
  }

  /* ----------------------------- PISTON EVENTS ------------------------------ */

  @EventHandler(ignoreCancelled = true)
  public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    piston(event, event.getBlocks());
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    piston(event, event.getBlocks());
  }

  private void piston(BlockPistonEvent event, List<Block> blocks) {
    var world = event.getBlock().getWorld();
    var bounds = Bounds3i.of(blocks);

    var waypoints = Waypoints.getInvulnerable(bounds, world);

    if (waypoints.isEmpty()) {
      return;
    }

    for (var b : blocks) {
      for (var w : waypoints) {
        if (w.getBounds().contains(b)) {
          event.setCancelled(true);
          return;
        }
      }
    }
  }
}