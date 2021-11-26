package net.forthecrown.utils.math;

import com.fastasyncworldedit.core.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import net.forthecrown.core.Crown;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

        //If it's too small to warrant using 3 threads, use one only
        if(totalArea < MIN_MULTI_THREAD) {
            mainThreadCopy(world, area, toMin);
            return;
        }

        int divided = area.getYSpan() / COUNT;
        int current = area.minY();

        //Create executors for the amount of threads
        for (int i = 0; i < COUNT; i++) {
            IntRange range = new IntRange(current, current += divided);
            COPY_EXECUTOR.execute(sectionRunnable(world, area, toMin, range));
        }
    }

    private static Runnable sectionRunnable(World world, BoundingBox area, WorldVec3i min, IntRange yRange) {
        return () -> {
            int maxY = yRange.getMaximumInteger() - yRange.getMinimumInteger();

            for (int x = 0; x < area.getXSpan(); x++) {
                for (int y = 0; y < maxY; y++) {
                    for (int z = 0; z < area.getZSpan(); z++) {
                        Vector3iOffset relative = new Vector3iOffset(x, y + yRange.getMinimumInteger(), z);

                        WorldVec3i destination = relative.apply(min);
                        WorldVec3i origin = relative.apply(area.minX(), area.minY(), area.minZ()).toWorldVector(world);

                        destination.getBlock().setBlockData(origin.getBlock().getBlockData(), false);
                    }
                }
            }
        };
    }

    static void copyPaste(World world, BoundingBox area, WorldVec3i destination) {
        copyPaste(world, area, destination, null, null);
    }

    static void copyPaste(World world, BoundingBox area, WorldVec3i destination, @Nullable Consumer<ForwardExtentCopy> copyConsumer, @Nullable Consumer<PasteBuilder> pasteConsumer) {
        CuboidRegion region = new CuboidRegion(
                BukkitAdapter.adapt(world),
                BlockVector3.at(area.minX(), area.minY(), area.minZ()),
                BlockVector3.at(area.maxX(), area.maxY(), area.maxZ())
        );
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession session = editSession(world)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    session, region, clipboard, region.getMinimumPoint()
            );

            if(copyConsumer != null) copyConsumer.accept(forwardExtentCopy);

            // configure here
            Operations.complete(forwardExtentCopy);
        }

        try (EditSession session = editSession(destination.getWorld())) {
            PasteBuilder builder = new ClipboardHolder(clipboard)
                    .createPaste(session)
                    .to(destination.toWE());

            if(pasteConsumer != null) pasteConsumer.accept(builder);

            Operations.complete(builder.build());
        }
    }

    static EditSession editSession(World world) {
        return new EditSession(new EditSessionBuilder(BukkitAdapter.adapt(world)));
    }

    /**
     * Same as {@link BoundingBoxes#copyTo(World, BoundingBox, WorldVec3i)} except it uses one thread instead of 3.
     * <p>{@link BoundingBoxes#copyTo(World, BoundingBox, WorldVec3i)} will call this method if the area is not large enough</p>
     * @param world The world to perform the copy in
     * @param area The area to copy
     * @param toMin The position to copy to
     */
    public static void mainThreadCopy(World world, BoundingBox area, WorldVec3i toMin) {
        Bukkit.getScheduler()
                .runTask(Crown.inst(),
                        sectionRunnable(world, area, toMin.clone().subtract(0, toMin.y, 0), new IntRange(area.minY(), area.maxY()))
                );
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

        for (int i = 0; i < amount; i++) {
            Vector3i boxStart = new Vector3i(
                    //This is essentially: x = starting X + index * direction * (distance + size)
                    start.getX() + (i * direction.getNormal().getX() * (size.getX() + distance)),
                    start.getY(),
                    start.getZ() + (i * direction.getNormal().getZ() * (size.getZ() + distance))
            );

            //Get the diagonal other end of the box with the size offset
            Vector3i boxEnd = size.apply(boxStart);

            //Set the box at the index to be the one we just calculated
            result[i] = new BoundingBox(boxStart.x, boxStart.y, boxStart.z, boxEnd.x, boxEnd.y, boxEnd.z);
        }

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
