package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.minecraft.core.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
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
    public void onBlockBreak(BlockBreakEvent event) { eventLogic(event.getPlayer(), event.getBlock(), event); }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) { eventLogic(event.getPlayer(), event.getBlock(), event); }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFromToEvent event) { eventLogic(null, event.getToBlock(), event); }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) { piston(event, event.getBlocks()); }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) { piston(event, event.getBlocks()); }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) { explode(event.blockList()); }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) { explode(event.blockList()); }

    private void explode(List<Block> blocks) {
        ListIterator<Block> iterator = blocks.listIterator();

        while (iterator.hasNext()) {
            Block b = iterator.next();
            PopulationRegion region = manager.get(RegionPos.toRelative(b.getX(), b.getZ()));
            BlockPos pos = ((CraftBlock) b).getPosition();

            if(region.getPoleBoundingBox().isInside(pos)) {
                iterator.remove();
            }
        }
    }

    private void piston(BlockPistonEvent event, List<Block> blocks) {
        for (Block b: blocks) {
            PopulationRegion region = manager.get(RegionPos.toRelative(b.getX(), b.getZ()));
            BlockPos pos = ((CraftBlock) b).getPosition();

            if(region.getPoleBoundingBox().isInside(pos)) {
                event.setCancelled(true);
            }
        }
    }

    private void eventLogic(Player player, Block block, Cancellable event) {
        if(player != null) {
            if(player.hasPermission(Permissions.REGIONS_ADMIN)) return;
            if(!player.getWorld().equals(Worlds.OVERWORLD)) return;
        }

        PopulationRegion region = manager.get(RegionPos.toRelative(block.getX(), block.getZ()));

        if(region.getPoleBoundingBox().isInside(new BlockPos(block.getX(), block.getY(), block.getZ()))) {
            event.setCancelled(true);
        }
    }

    private final RegionManager manager = Crown.getRegionManager();

    /*@EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        //if(event.isNewChunk()) return;

        int absoluteX = event.getChunk().getX() << 2;
        int absoluteZ = event.getChunk().getZ() << 2;

        RegionPos pos = RegionPos.toRelative(absoluteX, absoluteZ);
        RegionData data = manager.getData(pos);

        if(!chunkContainsPole(data, absoluteX, absoluteZ)) return;

        PopulationRegion region = manager.get(pos);
        manager.getGenerator().generate(region);
    }

    private boolean chunkContainsPole(RegionData data, int absoluteX, int absoluteZ) {
        int maxX = absoluteX + 16;
        int maxZ = absoluteZ + 16;

        BlockVector2 pos = data.getPolePosition();

        return MathUtil.isInRange(pos.getX(), absoluteX, maxX) &&
                MathUtil.isInRange(pos.getZ(), absoluteZ, maxZ);
    }*/
}
