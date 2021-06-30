package net.forthecrown.july.offset;

import net.forthecrown.core.utils.BlockPos;
import org.bukkit.Location;

public class BlockOffset {

    private final int xOffset;
    private final int zOffset;
    private final int yOffset;

    public BlockOffset(int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public static BlockOffset of(Location min, Location max){
        int x = max.getBlockX() - min.getBlockX();
        int y = max.getBlockY() - min.getBlockY();
        int z = max.getBlockZ() - min.getBlockZ();

        return new BlockOffset(x, y, z);
    }

    public static BlockOffset of(Location min, BlockPos max){
        int x = max.getX() - min.getBlockX();
        int y = max.getY() - min.getBlockY();
        int z = max.getZ() - min.getBlockZ();

        return new BlockOffset(x, y, z);
    }

    public static BlockOffset of(Location min, int x, int y, int z){
        return new BlockOffset(x - min.getBlockX(), y - min.getBlockY(), z - min.getBlockZ());
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getZOffset() {
        return zOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public Location apply(Location min){
        int x = min.getBlockX() + xOffset;
        int y = min.getBlockY() + yOffset;
        int z = min.getBlockZ() + zOffset;

        return new Location(min.getWorld(), x, y, z, min.getYaw(), min.getPitch());
    }
}
