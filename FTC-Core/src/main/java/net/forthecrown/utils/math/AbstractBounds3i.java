package net.forthecrown.utils.math;

import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public abstract class AbstractBounds3i<T extends AbstractBounds3i<T>> implements ImmutableBounds3i {
    protected int
        minX, minY, minZ,
        maxX, maxY, maxZ;

    protected final boolean immutable;

    protected AbstractBounds3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean immutable) {
        setBounds(minX, minY, minZ, maxX, maxY, maxZ);
        this.immutable = immutable;
    }

    protected AbstractBounds3i(int[] arr, boolean immutable) {
        this(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5], immutable);
    }

    protected abstract T getThis();
    protected abstract T cloneAt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean immutable);

    private void setBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(maxX, minX);
        this.maxY = Math.max(maxY, minY);
        this.maxZ = Math.max(maxZ, minZ);
    }

    public T set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if(immutable) {
            return cloneAt(minX, minY, minZ, maxX, maxY, maxZ, true);
        }

        setBounds(minX, minY, minZ, maxX, maxY, maxZ);
        return getThis();
    }

    public T mutable() {
        return immutable ? cloneAt(minX, minY, minZ, maxX, maxY, maxZ, false) : getThis();
    }

    public T immutable() {
        return immutable ? getThis() : cloneAt(minX, minY, minZ, maxX, maxY, maxZ, true);
    }

    public T expand(ImmutableVector3i vec3) { return expand(vec3.getX(), vec3.getY(), vec3.getZ()); }
    public T expand(int x, int y, int z) {
        return expand(x, y, z, x, y, z);
    }

    public T expand(Direction dir, int amount) {
        return expand(dir.getStepX(), dir.getStepY(), dir.getStepZ(), amount);
    }

    public T expand(BlockFace dir, int amount) {
        return expand(dir.getModX(), dir.getModY(), dir.getModZ(), amount);
    }

    public T expand(int dirX, int dirY, int dirZ, int amount) {
        return expand(
                -dirX * amount,
                -dirY * amount,
                -dirZ * amount,
                dirZ * amount,
                dirZ * amount,
                dirZ * amount
        );
    }

    public T expand(int val) { return expand(val, val, val, val, val, val); }
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

    public T contract(ImmutableVector3i vec3) { return contract(vec3.getX(), vec3.getY(), vec3.getZ()); }
    public T contract(int x, int y, int z) {
        return contract(x, y, z, x, y, z);
    }

    public T contract(Direction dir, int amount) {
        return contract(dir.getStepX(), dir.getStepY(), dir.getStepZ(), amount);
    }

    public T contract(BlockFace dir, int amount) {
        return contract(dir.getModX(), dir.getModY(), dir.getModZ(), amount);
    }

    public T contract(int dirX, int dirY, int dirZ, int amount) {
        return contract(
                -dirX * amount,
                -dirY * amount,
                -dirZ * amount,
                dirX * amount,
                dirY * amount,
                dirZ * amount
        );
    }

    public T contract(int val) { return contract(val, val, val, val, val, val); }
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

    public T combine(ImmutableBounds3i... others) {
        T result = clone();

        for (ImmutableBounds3i o: others) {
            result = result.combine(o);
        }

        return result;
    }

    public T combine(ImmutableBounds3i o) {
        return combine(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    public T combine(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return set(
                Math.min(this.minX, maxX),
                Math.min(this.minY, maxY),
                Math.min(this.minZ, maxZ),
                Math.max(this.maxX, minX),
                Math.max(this.maxY, minY),
                Math.max(this.maxZ, minZ)
        );
    }

    public T union(ImmutableVector3i vec) {
        return union(vec.getX(), vec.getY(), vec.getZ());
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

    public T intersection(ImmutableBounds3i o) {
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

    public BoundsFace[] getFaces() {
        Vector3i min = min();
        Vector3i max = max();

        return new BoundsFace[]{
                new BoundsFace(min, max.setY(minY), Direction.DOWN),
                new BoundsFace(min.setY(maxY), max, Direction.UP),
                new BoundsFace(min, max.setZ(minZ), Direction.NORTH),
                new BoundsFace(min.setY(maxZ), max, Direction.SOUTH),
                new BoundsFace(min, max.setX(minX), Direction.WEST),
                new BoundsFace(min.setX(maxX), max, Direction.EAST)
        };
    }

    public T move(Direction dir) {
        return move(dir, 1);
    }

    public T move(Direction dir, int amount) {
        return move(dir.getStepX(), dir.getStepY(), dir.getStepZ(), amount);
    }

    public T move(BlockFace dir) {
        return move(dir, 1);
    }

    public T move(BlockFace dir, int amount) {
        return move(dir.getModX(), dir.getModY(), dir.getModZ(), amount);
    }

    public T move(int xDir, int yDir, int zDir, int amount) {
        return move(
                xDir * amount,
                yDir * amount,
                zDir * amount
        );
    }

    public T move(ImmutableVector3i vec) {
        return move(vec.getX(), vec.getY(), vec.getZ());
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

    public boolean contains(ImmutableVector3i vec) {
        return contains(vec.getX(), vec.getY(), vec.getZ());
    }

    public boolean contains(BlockVector3 vec) {
        return contains(vec.getX(), vec.getY(), vec.getZ());
    }

    public boolean contains(Vector vec) {
        return contains(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }

    public boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }

    public boolean contains(Block block) {
        return contains(block.getX(), block.getY(), block.getZ());
    }

    public boolean contains(Location vec) {
        return contains(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
                z >= minZ && z <= maxZ &&
                y >= minY && y <= maxY;
    }

    public boolean overlaps(ImmutableBounds3i o) {
        return overlaps(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    @Override
    public boolean overlaps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return this.minX <= minX && this.maxX >= maxX
                && this.minY <= minY && this.maxY >= maxY
                && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    public boolean contains(ImmutableBounds3i o) {
        return contains(o.minX(), o.minY(), o.minZ(), o.maxX(), o.maxY(), o.maxZ());
    }

    @Override
    public boolean contains(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return this.minX <= minX && this.maxX >= maxX
                && this.minY <= minY && this.maxY >= maxY
                && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    @Override
    public int minX() {
        return minX;
    }

    public T setMinX(int minX) {
        return set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int minY() {
        return minY;
    }

    public T setMinY(int minY) {
        return set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int minZ() {
        return minZ;
    }

    public T setMinZ(int minZ) {
        return set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int maxX() {
        return maxX;
    }

    public T setMaxX(int maxX) {
        return set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int maxY() {
        return maxY;
    }

    public T setMaxY(int maxY) {
        return set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int maxZ() {
        return maxZ;
    }

    public T setMaxZ(int maxZ) {
        return set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BoundingBox toVanilla() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public org.bukkit.util.BoundingBox toBukkit() {
        return new org.bukkit.util.BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractBounds3i)) return false;

        AbstractBounds3i<?> i = (AbstractBounds3i<?>) o;

        if (minX != i.minX) return false;
        if (minY != i.minY) return false;
        if (minZ != i.minZ) return false;
        if (maxX != i.maxX) return false;
        if (maxY != i.maxY) return false;
        return maxZ == i.maxZ;
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
        return cloneAt(minX, minY, minZ, maxX, maxY, maxZ, immutable);
    }
}
