package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.utils.io.JsonUtils;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public class Bounds3i extends AbstractBounds3i<Bounds3i> implements Iterable<Vector3i> {
    public static final Bounds3i EMPTY = of(Vector3i.ZERO, Vector3i.ZERO);

    public Bounds3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    protected Bounds3i(int[] arr) {
        super(arr);
    }

    public static Bounds3i of(JsonElement element) {
        if (element.isJsonArray()) {
            return new Bounds3i(JsonUtils.readIntArray(element.getAsJsonArray()));
        }

        JsonObject obj = element.getAsJsonObject();
        return of(
                Vectors.read3i(obj.get("min")),
                Vectors.read3i(obj.get("max"))
        );
    }

    public static Bounds3i of(Tag t) {
        return new Bounds3i(((IntArrayTag) t).getAsIntArray());
    }

    public static Bounds3i of(Vector3i vec1, Vector3i vec2) {
        return new Bounds3i(vec1.x(), vec1.y(), vec1.z(), vec2.x(), vec2.y(), vec2.z());
    }

    public static Bounds3i of(Vector3i vec1, int radius) {
        return new Bounds3i(
                vec1.x() - radius,
                vec1.y() - radius,
                vec1.z() - radius,
                vec1.x() + radius,
                vec1.y() + radius,
                vec1.z() + radius
        );
    }

    public static Bounds3i of(Region region) {
        return of(
                Vectors.from(region.getMinimumPoint()),
                Vectors.from(region.getMaximumPoint())
        );
    }

    public static Bounds3i of(AbstractBounds3i bounds3i) {
        if (bounds3i instanceof Bounds3i b) {
            return b;
        }

        return new Bounds3i(
                bounds3i.minX(), bounds3i.minY(), bounds3i.minZ(),
                bounds3i.maxX(), bounds3i.maxY(), bounds3i.maxZ()
        );
    }

    public static Bounds3i of(Iterable<Block> blocks) {
        var it = blocks.iterator();

        if (!it.hasNext()) {
            return EMPTY;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        while (it.hasNext()) {
            var b = it.next();

            minX = Math.min(minX, b.getX());
            minY = Math.min(minY, b.getY());
            minZ = Math.min(minZ, b.getZ());

            maxX = Math.max(maxX, b.getX());
            maxY = Math.max(maxY, b.getY());
            maxZ = Math.max(maxZ, b.getZ());
        }

        return new Bounds3i(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Bounds3i of(BoundingBox box) {
        return of(
                Vectors.doubleFrom(box.getMin()),
                Vectors.doubleFrom(box.getMax())
        );
    }

    public static Bounds3i of(double minX, double minY, double minZ,
                              double maxX, double maxY, double maxZ
    ) {
        return of(
                Vector3d.from(minX, minY, minZ),
                Vector3d.from(maxX, maxY, maxZ)
        );
    }

    public static Bounds3i of(Vector3d min, Vector3d max) {
        return of(
                min.floor().toInt(),
                max.ceil().toInt()
        );
    }

    @Override
    protected Bounds3i cloneAt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new Bounds3i(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public WorldBounds3i toWorldBounds(World world) {
        return new WorldBounds3i(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @NotNull
    @Override
    public VectorIterator iterator() {
        return new VectorIterator(min(), max(), volume());
    }

    @Override
    public String toString() {
        return "(" + super.toString() + ")";
    }
}