package net.forthecrown.regions;

import net.forthecrown.utils.math.FtcRegion;
import org.bukkit.World;

public class PoleRegion {
    private final RegionCords cords;
    private final FtcRegion region;

    public PoleRegion(RegionCords cords, World world) {
        this.cords = cords;
        this.region = makeRegion(world);
    }

    private FtcRegion makeRegion(World world) {
        int minX = cords.getAbsoluteX() - RegionCords.HALF_REGION_SIZE;
        int minZ = cords.getAbsoluteZ() - RegionCords.HALF_REGION_SIZE;

        int maxX = cords.getAbsoluteX() + RegionCords.HALF_REGION_SIZE;
        int maxZ = cords.getAbsoluteZ() + RegionCords.HALF_REGION_SIZE;

        return new FtcRegion(world, minX, -65, minZ, maxX, 312, maxZ);
    }

    public RegionCords getCords() {
        return cords;
    }

    public FtcRegion getRegion() {
        return region;
    }
}
