package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.utils.Worlds;
import net.minecraft.core.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class RegionsListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        eventLogic(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        eventLogic(event.getPlayer(), event.getBlock(), event);
    }

    private void eventLogic(Player player, Block block, Cancellable event) {
        if(player.hasPermission(Permissions.REGIONS_ADMIN)) return;
        if(!player.getWorld().equals(Worlds.OVERWORLD)) return;

        PopulationRegion region = Crown.getRegionManager().get(RegionPos.fromAbsolute(block.getX(), block.getZ()));

        if(region.getPoleBoundingBox().isInside(new BlockPos(block.getX(), block.getY(), block.getZ()))) {
            event.setCancelled(true);
        }
    }

    /*private final RegionManager manager = Crown.getRegionManager();

    @EventHandler(ignoreCancelled = true)
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
