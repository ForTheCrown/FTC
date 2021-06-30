package net.forthecrown.july.offset;

import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.july.EventConstants;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class BoundingBoxOffset {

    private final BlockOffset minOffset;
    private final BlockOffset maxOffset;

    public BoundingBoxOffset(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
    }

    public BoundingBoxOffset(BoundingBox box){
        this(EventConstants.minLoc(), box);
    }

    public BoundingBoxOffset(Location minLoc, BoundingBox box){
        this.minOffset = BlockOffset.of(minLoc, box.getMin().toLocation(minLoc.getWorld()));
        this.maxOffset = BlockOffset.of(minLoc, box.getMax().toLocation(minLoc.getWorld()));
    }

    public BlockOffset getMinOffset() {
        return minOffset;
    }

    public BlockOffset getMaxOffset() {
        return maxOffset;
    }

    public CrownBoundingBox apply(Location minLoc){
        Location min = minOffset.apply(minLoc);
        Location max = maxOffset.apply(minLoc);

        return CrownBoundingBox.of(min, max);
    }
}
