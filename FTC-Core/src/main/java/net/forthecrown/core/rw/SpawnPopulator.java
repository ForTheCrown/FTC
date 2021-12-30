package net.forthecrown.core.rw;

import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SpawnPopulator extends BlockPopulator {
    private boolean alreadyCalled;

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull LimitedRegion region) {
        if(alreadyCalled) return;
        alreadyCalled = true;

        Vector3i start = findStart(region, worldInfo);
        generate(start, region::setBlockData);
    }

    @FunctionalInterface
    public interface BlockPlacer {
        void place(int x, int y, int z, BlockData data);

        default void place(Vector3i pos, BlockData data) {
            place(pos.getX(), pos.getY(), pos.getZ(), data);
        }
    }

    public static void generate(Vector3i start, BlockPlacer placer) {

        // floor
        setArea(start, start.clone().add(12, 0, 12), placer, Material.DIORITE_SLAB.createBlockData());
        setArea(start.clone().add(2, 0, 2), start.clone().subtract(2, 0, 2), placer, Material.SMOOTH_STONE_SLAB.createBlockData());

        // surrounding logs
        placeLogs(start, BlockFace.SOUTH, 3, placer);
        placeLogs(start, BlockFace.EAST, 3, placer);
    }

    private static void placeLogs(Vector3i start, BlockFace dir, int amount, BlockPlacer placer) {
         Orientable orientable = (Orientable) Material.STRIPPED_DARK_OAK_LOG.createBlockData();
         orientable.setAxis(fromFace(dir));

         setRow(start, dir, amount, placer, orientable);
    }

    private static Axis fromFace(BlockFace face) {
        if(face.getModX() != 0) return Axis.X;
        if(face.getModY() != 0) return Axis.Y;
        return Axis.Z;
    }

    private static void setRow(Vector3i start, BlockFace dir, int amount, BlockPlacer placer, BlockData data) {
        for (int i = 1; i <= amount; i++) {
            placer.place(
                    (start.getX() + i) * dir.getModX(),
                    (start.getY() + i) * dir.getModY(),
                    (start.getZ() + i) * dir.getModZ(),
                    data
            );
        }
    }

    private static void setArea(Vector3i min, Vector3i max, BlockPlacer placer, BlockData data) {
        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    placer.place(x, y, z, data);
                }
            }
        }
    }

    Vector3i findStart(LimitedRegion region, WorldInfo info) {
        int
                x = (region.getCenterChunkX() << 4) + 2,
                y = info.getMaxHeight(),
                z = (region.getCenterBlockZ() << 4) + 2;

        for (int i = y; i > info.getMinHeight(); i--) {
            if(!region.getType(x, i, z).isAir()) return new Vector3i(x, i + 1, z);
        }

        return null;
    }
}
