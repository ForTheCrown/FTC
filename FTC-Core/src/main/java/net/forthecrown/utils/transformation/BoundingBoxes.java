package net.forthecrown.utils.transformation;

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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.Vector3iOffset;
import net.forthecrown.utils.math.WorldVec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
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
    static final ExecutorService COPY_EXECUTOR = Executors.newFixedThreadPool(COUNT);

    //The minimum area of a bounding box for it to warrant using 3 threads.
    static final int MIN_MULTI_THREAD = 15 * 15 * 15;

    /**
     * Copies a bounding box to a given location
     * @param world The world to perform the action in
     * @param area The area to copy
     * @param toMin The copy paste
     */
    public static void copyTo(World world, BoundingBox area, WorldVec3i toMin) {
        RegionCopyPaste paste = createPaste(world, area, toMin);
        paste.run();
    }

    public static RegionCopyPaste createPaste(World world, BoundingBox area, WorldVec3i toMin) {
        return new RegionCopyPaste(
                toMin.getWorld(), toMin.toNonWorld(),
                FtcBoundingBox.of(world, area)
        );
    }

    public static void copyPaste(World world, BoundingBox area, WorldVec3i destination) {
        copyPaste(world, area, destination, null, null);
    }

    public static void copyPaste(World world, BoundingBox area, WorldVec3i destination, @Nullable Consumer<ForwardExtentCopy> copyConsumer, @Nullable Consumer<PasteBuilder> pasteConsumer) {
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
        RegionCopyPaste paste = createPaste(world, area, toMin);
        paste.runSync();
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
            result[i] = new BoundingBox(boxStart.getX(), boxStart.getY(), boxStart.getZ(), boxEnd.getX(), boxEnd.getY(), boxEnd.getZ());
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

    public static BoundingBox regionToNms(BlockVector3 min, BlockVector3 max) {
        return new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public static BoundingBox regionToNms(Region region) {
        return regionToNms(region.getMinimumPoint(), region.getMaximumPoint());
    }

    public static BoundingBox wgToNms(ProtectedRegion region) {
        return regionToNms(region.getMinimumPoint(), region.getMaximumPoint());
    }
    
    public static boolean overlaps(BoundingBox b1, BoundingBox b2) {
        for (BlockPos p: corners(b1)) {
            if(b2.isInside(p)) return true;
        }

        return false;
    }
    
    public static BlockPos[] corners(BoundingBox b) {
        return new BlockPos[] {
                new BlockPos(b.maxX(), b.maxY(), b.maxZ()),
                new BlockPos(b.minX(), b.maxY(), b.maxZ()),
                new BlockPos(b.maxX(), b.minY(), b.maxZ()),
                new BlockPos(b.minX(), b.minY(), b.maxZ()),
                new BlockPos(b.maxX(), b.maxY(), b.minZ()),
                new BlockPos(b.minX(), b.maxY(), b.minZ()),
                new BlockPos(b.maxX(), b.minY(), b.minZ()),
                new BlockPos(b.minX(), b.minY(), b.minZ())
        };
    }

    public static IntArrayTag save(BoundingBox box) {
        return new IntArrayTag(toIntArray(box));
    }

    public static BoundingBox load(Tag tag) {
        IntArrayTag intArr = (IntArrayTag) tag;

        return ofIntArray(intArr.getAsIntArray());
    }

    public static int[] toIntArray(BoundingBox box) {
        return new int[] {
                box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ()
        };
    }

    public static BoundingBox ofIntArray(int... ints) {
        return new BoundingBox(
                ints[0], ints[1], ints[2],
                ints[3], ints[4], ints[5]
        );
    }
}
