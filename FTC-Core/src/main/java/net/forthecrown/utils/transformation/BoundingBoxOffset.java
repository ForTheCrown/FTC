package net.forthecrown.utils.transformation;

import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.Vector3iOffset;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * Similar to {@link Vector3iOffset} except for Bounding boxes.
 */
public class BoundingBoxOffset {

    private final Vector3iOffset minOffset;
    private final Vector3iOffset maxOffset;

    public BoundingBoxOffset(Vector3iOffset minOffset, Vector3iOffset maxOffset) {
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
    }

    public static BoundingBoxOffset of(Location defMinLoc, int x, int y, int z, int maxX, int maxY, int maxZ){
        return of(defMinLoc, new BoundingBox(x, y, z, maxX, maxY, maxZ));
    }

    public static BoundingBoxOffset of(Location defMinLoc, BoundingBox boundingBox){
        return new BoundingBoxOffset(
                Vector3iOffset.of(defMinLoc, boundingBox.getMin().toLocation(defMinLoc.getWorld())),
                Vector3iOffset.of(defMinLoc, boundingBox.getMax().toLocation(defMinLoc.getWorld()))
        );
    }

    public Vector3iOffset getMinOffset() {
        return minOffset;
    }

    public Vector3iOffset getMaxOffset() {
        return maxOffset;
    }

    public FtcBoundingBox apply(Location minLoc){
        Location min = minOffset.apply(minLoc);
        Location max = maxOffset.apply(minLoc);

        return FtcBoundingBox.of(min, max);
    }

    public BoundingBox apply(Vector vector){
        Vector min = minOffset.apply(vector);
        Vector max = maxOffset.apply(vector);

        return BoundingBox.of(min, max);
    }

    public BoundingBox apply(Vector3i pos){
        return apply(new Vector(pos.x, pos.y, pos.z));
    }

    public BoundingBox apply(double x, double y, double z) {
        Vector min = minOffset.apply(x, y, z);
        Vector max = maxOffset.apply(x, y, z);

        return BoundingBox.of(min, max);
    }
}
