package net.forthecrown.utils;

import net.forthecrown.utils.math.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R1.generator.CraftLimitedRegion;
import org.bukkit.generator.LimitedRegion;

/**
 * Represents something which can place a
 * block at set of given coordinates.
 */
public interface BlockPlacer {

    /**
     * Places a block at the absolute coordinates given
     * @param x The X cord
     * @param y The Y cord
     * @param z The Z cord
     * @param data The block data to set
     */
    void place(int x, int y, int z, FtcBlockData data);

    default void place(Vector3i pos, FtcBlockData data) {
        place(pos.getX(), pos.getY(), pos.getZ(), data);
    }

    default void place(int x, int y, int z, BlockData data) {
        place(x, y, z, FtcBlockData.of(data));
    }

    default void place(int x, int y, int z, TileState state) {
        place(x, y, z, FtcBlockData.of(state));
    }

    default void place(Vector3i p, BlockData data) {
        place(p.getX(), p.getY(), p.getZ(), data);
    }

    default void place(Vector3i p, TileState state) {
        place(p.getX(), p.getY(), p.getZ(), state);
    }

    /**
     * Creates a block placer for the given world
     * @param world The world to create the placer for
     * @return The created placer
     */
    static BlockPlacer world(World world) {
        return (x, y, z, data) -> {
            world.setBlockData(x, y, z, data.getData());

            BlockState state = world.getBlockState(x, y, z);
            if(data.getTag() != null && state instanceof TileState tileState) {
                BlockEntity entity = Bukkit2NMS.getBlockEntity(tileState);
                entity.load(data.getTag());

                Bukkit2NMS.getLevel(world).setBlockEntity(entity);
            }
        };
    }

    /**
     * Creates a block placer for world generation time
     * placement
     * @param region The limited region the placer can use
     * @return The created block placer.
     */
    static BlockPlacer populator(LimitedRegion region) {
        return new BlockPlacer() {
            @Override
            public void place(int x, int y, int z, FtcBlockData data) {
                // Set the block data
                region.setBlockData(x, y, z, data.getData());

                // If we have a tag to apply,
                // apply it using NMS
                if(data.getTag() != null) {
                    CraftLimitedRegion limitedRegion = (CraftLimitedRegion) region;
                    BlockEntity entity = limitedRegion.getHandle().getBlockEntity(new BlockPos(x , y, z));
                    entity.load(data.getTag());
                }
            }
        };
    }
}
