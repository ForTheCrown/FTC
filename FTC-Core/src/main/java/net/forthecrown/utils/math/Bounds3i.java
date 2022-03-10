package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.utils.JsonUtils;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class Bounds3i extends AbstractBounds3i<Bounds3i> implements Iterable<Vector3i> {
    public Bounds3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ, true);
    }

    protected Bounds3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean immutable) {
        super(minX, minY, minZ, maxX, maxY, maxZ, immutable);
    }

    protected Bounds3i(int[] arr, boolean immutable) {
        super(arr, immutable);
    }

    public static Bounds3i of(JsonElement element) {
        return new Bounds3i(JsonUtils.readIntArray(element.getAsJsonArray()), true);
    }

    public static Bounds3i of(Tag t) {
        return new Bounds3i(((IntArrayTag) t).getAsIntArray(), true);
    }

    public static Bounds3i of(ImmutableVector3i vec1, ImmutableVector3i vec2) {
        return new Bounds3i(vec1.getX(), vec1.getY(), vec1.getZ(), vec2.getX(), vec2.getY(), vec2.getZ());
    }

    public static Bounds3i of(ImmutableVector3i vec1, int radius) {
        return new Bounds3i(
                vec1.getX() - radius,
                vec1.getY() - radius,
                vec1.getZ() - radius,
                vec1.getX() + radius,
                vec1.getY() + radius,
                vec1.getZ() + radius
        );
    }

    public static Bounds3i of(Region region) {
        return of(
                Vector3i.of(region.getMinimumPoint()),
                Vector3i.of(region.getMaximumPoint())
        );
    }

    @Override
    protected Bounds3i getThis() {
        return this;
    }

    @Override
    protected Bounds3i cloneAt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean immutable) {
        return new Bounds3i(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public WorldBounds3i toWorldBounds(World world) {
        return new WorldBounds3i(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @NotNull
    @Override
    public VectorIterator<Vector3i> iterator() {
        return new VectorIterator<>(min(), max(), volume());
    }
}
