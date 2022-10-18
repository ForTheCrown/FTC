package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.utils.io.JsonUtils;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3i;

public class Bounds3i extends AbstractBounds3i<Bounds3i> implements Iterable<Vector3i> {

    public Bounds3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ, true);
    }

    protected Bounds3i(int[] arr, boolean immutable) {
        super(arr, immutable);
    }

    public static Bounds3i of(JsonElement element) {
        if (element.isJsonArray()) {
            return new Bounds3i(JsonUtils.readIntArray(element.getAsJsonArray()), true);
        }

        JsonObject obj = element.getAsJsonObject();
        return of(
                Vectors.read3i(obj.get("min")),
                Vectors.read3i(obj.get("max"))
        );
    }

    public static Bounds3i of(Tag t) {
        return new Bounds3i(((IntArrayTag) t).getAsIntArray(), true);
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
        var min = region.getMinimumPoint();
        var max = region.getMaximumPoint();
        return of(
                Vectors.from(region.getMinimumPoint()),
                Vectors.from(region.getMaximumPoint())
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
    public VectorIterator iterator() {
        return new VectorIterator(min(), max(), volume());
    }

    @Override
    public String toString() {
        return "(" + super.toString() + ")";
    }
}