package net.forthecrown.utils.math;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class BoundingBoxOffset {

    private final BlockOffset minOffset;
    private final BlockOffset maxOffset;

    public BoundingBoxOffset(Location defMinLoc, BoundingBox box){
        this.minOffset = BlockOffset.of(defMinLoc, box.getMin().toLocation(defMinLoc.getWorld()));
        this.maxOffset = BlockOffset.of(defMinLoc, box.getMax().toLocation(defMinLoc.getWorld()));
    }

    public static BoundingBoxOffset of(Location defMinLoc, int x, int y, int z, int maxX, int maxY, int maxZ){
        return new BoundingBoxOffset(defMinLoc, new BoundingBox(x, y, z, maxX, maxY, maxZ));
    }

    public static BoundingBoxOffset of(Location defMinLoc, BoundingBox boundingBox){
        return new BoundingBoxOffset(defMinLoc, boundingBox);
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

    public BoundingBox apply(Vector vector){
        Vector min = minOffset.apply(vector);
        Vector max = maxOffset.apply(vector);

        return BoundingBox.of(min, max);
    }

    public BoundingBox apply(BlockPos pos){
        return apply(new Vector(pos.x, pos.y, pos.z));
    }
}
