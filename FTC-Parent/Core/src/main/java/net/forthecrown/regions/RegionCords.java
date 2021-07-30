package net.forthecrown.regions;

import net.forthecrown.utils.math.BlockPos;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Location;

public class RegionCords {
    public static final int REGION_SIZE = 400;
    public static final int HALF_REGION_SIZE = REGION_SIZE / 2;

    private final int x;
    private final int z;

    public RegionCords(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getAbsoluteX() {
        return toAbsolute(x);
    }

    public int getAbsoluteZ() {
        return toAbsolute(z);
    }

    private int toAbsolute(int relative) {
        return relative * REGION_SIZE + HALF_REGION_SIZE;
    }

    public static RegionCords of(Location loc) {
        return fromAbsolute(loc.getBlockX(), loc.getBlockZ());
    }

    public static RegionCords of(Vec2 vec2) {
        return fromAbsolute((int) vec2.x, (int) vec2.y);
    }

    public static RegionCords of(BlockPos pos) {
        return fromAbsolute(pos.getX(), pos.getZ());
    }

    public static RegionCords fromAbsolute(int x, int z) {
        return new RegionCords(
                fromAbsoluteCord(x),
                fromAbsoluteCord(z)
        );
    }

    private static int fromAbsoluteCord(int cord) {
        return (cord - HALF_REGION_SIZE) / REGION_SIZE;
    }
}
