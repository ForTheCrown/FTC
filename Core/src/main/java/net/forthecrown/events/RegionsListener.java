package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.minecraft.core.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
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
    public void onChunkLoad(PlayerChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        //Get block cords from chunk cords
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;

        //Get pos from those cords
        RegionPos pos = RegionPos.fromAbsolute(x, z);
        PopulationRegion region = manager.get(pos);

        //Get chunk's 2D bounding box
        BoundingBox2D chunkRegion = new BoundingBox2D(x, z, x + 16, z + 16);

        //If it doesn't contain the region pole, stop
        if(!chunkRegion.contains(region.getPolePosition())) return;

        //If it does, however, generate a region pole
        manager.getGenerator().generate(region);
    }

    private static class BoundingBox2D {
        private final int minX;
        private final int minZ;
        private final int maxX;
        private final int maxZ;

        private BoundingBox2D(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        public boolean contains(BlockVector2 vec2) {
            int x = vec2.getX();
            int z = vec2.getZ();

            return MathUtil.isInRange(x, minX, maxX) && MathUtil.isInRange(z, minZ, maxZ);
        }
    }*/
}
