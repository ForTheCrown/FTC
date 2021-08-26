package net.forthecrown.utils.math;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.Announcer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.World;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class for utility and other methods relating to bounding boxes.
 */
public final class BoundingBoxes {
    private BoundingBoxes() {}

    //The thread amount
    static final byte COUNT = 3;

    //Threads
    static final Executor COPY_EXECUTOR = Executors.newFixedThreadPool(COUNT);

    //The minimum area of a bounding box for it to warrant using 3 threads.
    static final int MIN_MULTI_THREAD = 15 * 15 * 15;

    /**
     * Copies a bounding box to a given location
     * @param world The world to perform the action in
     * @param area The area to copy
     * @param toMin The copy destination
     */
    public static void copyTo(World world, BoundingBox area, WorldVec3i toMin) {
        int totalArea = area.getXSpan() * area.getYSpan() * area.getZSpan();
        Announcer.debug("copyTo toMin: " + toMin);

        //If it's too small to warrant using 3 threads, use one only
        if(totalArea < MIN_MULTI_THREAD) {
            mainThreadCopy(world, area, toMin);
            return;
        }

        int divided = area.getYSpan() / COUNT;
        int current = area.minY();

        //Create executors for the amount of threads
        for (int i = 0; i < COUNT; i++) {
            IntRange range = new IntRange(current, current += divided-1);
            COPY_EXECUTOR.execute(sectionRunnable(world, area, toMin, range));
        }
    }

    private static Runnable sectionRunnable(World world, BoundingBox area, WorldVec3i min, IntRange yRange) {
        Announcer.debug("sectionRunnable min:" + min);

        return () -> {
            int maxY = yRange.getMaximumInteger() - yRange.getMinimumInteger();

            for (int x = 0; x < area.getXSpan(); x++) {
                for (int y = 0; y < maxY; y++) {
                    for (int z = 0; z < area.getZSpan(); z++) {
                        Vector3iOffset relative = new Vector3iOffset(x, y + yRange.getMinimumInteger(), z);

                        WorldVec3i destination = relative.apply(min);
                        WorldVec3i origin = relative.apply(area.minX(), area.minY(), area.minZ()).toWorldVector(world);

                        destination.getBlock().setBlockData(origin.getBlock().getBlockData().clone());
                    }
                }
            }
        };
    }

    /**
     * Same as {@link BoundingBoxes#copyTo(World, BoundingBox, WorldVec3i)} except it uses one thread instead of 3.
     * <p>{@link BoundingBoxes#copyTo(World, BoundingBox, WorldVec3i)} will call this method if the area is not large enough</p>
     * @param world The world to perform the copy in
     * @param area The area to copy
     * @param toMin The position to copy to
     */
    public static void mainThreadCopy(World world, BoundingBox area, WorldVec3i toMin) {
        sectionRunnable(world, area, toMin, new IntRange(area.minY(), area.maxY())).run();
    }

    /**
     * Creates an array of bounding boxes.
     * @param start The starting position to create bounding boxes from
     * @param size The size of the bounding boxes
     * @param direction The direction to create the boxes in
     * @param distance The distance between each box
     * @param amount The amount of boxes to create
     * @return The created boxes
     */
    public static BoundingBox[] createArray(Vector3i start, Vector3iOffset size, Direction direction, int distance, int amount) {
        BoundingBox[] result = new BoundingBox[amount];

        Crown.logger().info("start: " + start);

        for (int i = 0; i < amount; i++) {
            Vector3i boxStart = new Vector3i(
                    //This is essentially: x = starting X + index * direction * (distance + size)
                    start.getX() + (i * direction.getNormal().getX() * (size.getX() + distance)),
                    start.getY(),
                    start.getZ() + (i * direction.getNormal().getZ() * (size.getZ() + distance))
            );

            Crown.logger().info("boxStart: " + boxStart);
            Crown.logger().info("index: " + i);

            //Get the diagonal other end of the box with the size offset
            Vector3i boxEnd = size.apply(boxStart);
            Crown.logger().info("end: " + boxEnd);

            //Set the box at the index to be the one we just calculated
            result[i] = new BoundingBox(boxStart.x, boxStart.y, boxStart.z, boxEnd.x, boxEnd.y, boxEnd.z);
        }

        Crown.logger().info(Arrays.toString(result));
        return result;
    }

    /**
     * Turns a given WorldEdit region into a bounding box
     * @param region The region to convert
     * @return The converted bounding box
     */
    public static BoundingBox fromRegion(Region region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }
}
