package net.forthecrown.events;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.Waypoints;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Set;

public class WaypointListener implements Listener {
    /* ----------------------------- REGULAR BLOCK EVENTS ------------------------------ */

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        cancel(event.getBlock(), event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        cancel(event.getBlock(), event::setCancelled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        cancel(event.getToBlock(), event::setCancelled);
    }

    private void cancel(Block block, BooleanConsumer setCancelled) {
        Vector3i pos = Vectors.from(block);

        World world = block.getWorld();
        Set<Waypoint> waypoints = Waypoints.getInvulnerable(pos, world);

        if (waypoints.isEmpty()) {
            return;
        }

        setCancelled.accept(true);
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

            for (var w: waypoints) {
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

        for (var b: blocks) {
            for (var w: waypoints) {
                if (w.getBounds().contains(b)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}