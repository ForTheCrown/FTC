package net.forthecrown.utils.math;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.JsonSerializable;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import static org.spongepowered.math.GenericMath.clamp;

public abstract class AbstractBounds3i<T extends AbstractBounds3i<T>> implements JsonSerializable {
    @Getter
    @Accessors(fluent = true)
    protected final int
        minX, minY, minZ,
        maxX, maxY, maxZ;

    protected AbstractBounds3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(maxX, minX);
        this.maxY = Math.max(maxY, minY);
        this.maxZ = Math.max(maxZ, minZ);
    }

    protected AbstractBounds3i(int[] arr) {
        this(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
    }

    protected abstract T cloneAt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    public T set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return cloneAt(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public T expand(Vector3i vec3) {
        return expand(vec3.x(), vec3.y(), vec3.z());
    }

    public T expand(int x, int y, int z) {
        return expand(x, y, z, x, y, z);
    }

    public T expand(int val) { 
        return expand(val, val, val, val, val, val); 
    }

    public T expand(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return set(
                this.minX - minX,
                this.minY - minY,
                this.minZ - minZ,
                this.maxX + maxX,
                this.maxY + maxY,
                this.maxZ + maxZ
        );
    }

    public T contract(Vector3i vec3) {
        return contract(vec3.x(), vec3.y(), vec3.z());
    }

    public T contract(int x, int y, int z) {
        return contract(x, y, z, x, y, z);
    }

    public T contract(int val) { 
        return contract(val, val, val, val, val, val); 
    }

    public T contract(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return set(
                this.minX + minX,
                this.minY + minY,
                this.minZ + minZ,
                this.maxX - maxX,
                this.maxY - maxY,
                this.maxZ - maxZ
        );
    }

    public T combine(AbstractBounds3i... others) {
        T result = clone();

        for (AbstractBounds3i<T> o: others) {
            result = result.combine(o);
        }

        return result;
    }

    public T combine(AbstractBounds3i o) {
        return combine(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    public T combine(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return set(
                Math.min(this.minX, minX),
                Math.min(this.minY, minY),
                Math.min(this.minZ, minZ),
                Math.max(this.maxX, maxX),
                Math.max(this.maxY, maxY),
                Math.max(this.maxZ, maxZ)
        );
    }

    public T union(Vector3i vec) {
        return union(vec.x(), vec.y(), vec.z());
    }

    public T union(int x, int y, int z) {
        return set(
                Math.min(minX, x),
                Math.min(minY, y),
                Math.min(minZ, z),
                Math.max(maxZ, z),
                Math.max(maxZ, z),
                Math.max(maxZ, z)
        );
    }

    public T intersection(AbstractBounds3i o) {
        return intersection(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    public T intersection(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Validate.isTrue(overlaps(minX, minY, minZ, maxX, maxY, maxZ), "Given region does not overlap");

        return set(
                Math.max(this.minX, minX),
                Math.max(this.minY, minY),
                Math.max(this.minZ, minZ),
                Math.min(this.maxX, maxX),
                Math.min(this.maxY, maxY),
                Math.min(this.maxZ, maxZ)
        );
    }

    public T move(Vector3i vec) {
        return move(vec.x(), vec.y(), vec.z());
    }

    public T move(int x, int y, int z) {
        return set(
                this.minX + x,
                this.minY + y,
                this.minZ + z,
                this.maxX + x,
                this.maxY + y,
                this.maxZ + z
        );
    }

    public boolean contains(Vector3i vec) {
        return contains(vec.x(), vec.y(), vec.z());
    }

    public boolean contains(Entity entity) {
        return overlaps(entity.getBoundingBox());
    }

    public boolean contains(Block block) {
        return contains(block.getX(), block.getY(), block.getZ());
    }

    public boolean contains(Location vec) {
        return contains(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
                z >= minZ && z <= maxZ &&
                y >= minY && y <= maxY;
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX &&
                z >= minZ && z <= maxZ &&
                y >= minY && y <= maxY;
    }

    public boolean overlaps(org.bukkit.util.BoundingBox box) {
        return overlaps(Bounds3i.of(box));
    }

    public boolean overlaps(AbstractBounds3i o) {
        return overlaps(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    public boolean overlaps(int minX, int minY, int minZ,
                            int maxX, int maxY, int maxZ
    ) {
        return this.minX < maxX && this.maxX >= minX
            && this.minY < maxY && this.maxY >= minY
            && this.minZ < maxZ && this.maxZ >= minZ;
    }

    public boolean overlaps(double minX, double minY, double minZ,
                            double maxX, double maxY, double maxZ
    ) {
        return ((double) this.minX) < maxX && ((double) this.maxX) + 1.0D > minX
                && ((double) this.minY) < maxY && ((double) this.maxY) + 1.0D > minY
                && ((double) this.minZ) < maxZ && ((double) this.maxZ) + 1.0D > minZ;
    }

    public boolean contains(AbstractBounds3i o) {
        return contains(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    public boolean contains(int minX, int minY, int minZ,
                            int maxX, int maxY, int maxZ
    ) {
        return this.minX <= minX && this.maxX >= maxX
                && this.minY <= minY && this.maxY >= maxY
                && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    public org.bukkit.util.BoundingBox toBukkit() {
        return new org.bukkit.util.BoundingBox(
                minX,
                minY,
                minZ,

                maxX,
                maxY,
                maxZ
        );
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractBounds3i<?> i)) return false;

        return maxZ == i.maxZ
                && minX != i.minX
                && minY != i.minY
                && minZ != i.minZ
                && maxX != i.maxX
                && maxY != i.maxY;
    }

    @Override
    public int hashCode() {
        int result = minX;
        result = 31 * result + minY;
        result = 31 * result + minZ;
        result = 31 * result + maxX;
        result = 31 * result + maxY;
        result = 31 * result + maxZ;
        return result;
    }

    @Override
    public String toString() {
        return min().toString() + ", " + max().toString();
    }

    @Override
    public T clone() {
        return cloneAt(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int sizeX() {
        return (maxX() - minX()) + 1;
    }

    public int sizeY() {
        return (maxY() - minY()) + 1;
    }

    public int sizeZ() {
        return (maxZ() - minZ()) + 1;
    }

    public int spanX() {
        return maxX() - minX();
    }

    public int spanY() {
        return maxY() - minY();
    }

    public int spanZ() {
        return maxZ() - minZ();
    }

    public double centerX() {
        return minX() + ((double) sizeX() / 2);
    }

    public double centerY() {
        return minY() + ((double) sizeY() / 2);
    }

    public double centerZ() {
        return minZ() + ((double) sizeZ() / 2);
    }

    public long volume() {
        return (long) sizeX() * sizeY() * sizeZ();
    }

    public Vector3d center() {
        return Vector3d.from(centerX(), centerY(), centerZ());
    }

    public Vector3i dimensions() {
        return new Vector3i(spanX(), spanY(), spanZ());
    }

    public Vector3i size() {
        return new Vector3i(sizeX(), sizeY(), sizeZ());
    }

    public Vector3i min() {
        return new Vector3i(minX(), minY(), minZ());
    }

    public Vector3i max() {
        return new Vector3i(maxX(), maxY(), maxZ());
    }

    public int[] toIntArray() {
        return new int[] { minX(), minY(), minZ(), maxX(), maxY(), maxZ() };
    }

    public Vector3d getClosestPosition(Vector3d pos) {
        return Vector3d.from(
                clamp(pos.x(), minX, maxX),
                clamp(pos.y(), minY, maxY),
                clamp(pos.z(), minZ, maxZ)
        );
    }

    @Override
    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.add("min", Vectors.writeJson(min()));
        obj.add("max", Vectors.writeJson(max()));
        return obj;
    }

    public Tag save() {
        return new IntArrayTag(toIntArray());
    }
}