package net.forthecrown.utils.math;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import github.scarsz.discordsrv.dependencies.commons.lang3.builder.HashCodeBuilder;
import net.minecraft.core.Vec3i;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public abstract class AbstractVector3i<T extends AbstractVector3i<T>> implements ImmutableVector3i {
    public int x;
    public int y;
    public int z;

    public AbstractVector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public AbstractVector3i() {
    }

    protected abstract T getThis();
    protected abstract T cloneAt(int x, int y, int z);

    public T set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return getThis();
    }

    public int getX() {
        return x;
    }

    public T setX(int x) {
        this.x = x;
        return getThis();
    }

    public int getY() {
        return y;
    }

    public T setY(int y) {
        this.y = y;
        return getThis();
    }

    public int getZ() {
        return z;
    }

    public T setZ(int z) {
        this.z = z;
        return getThis();
    }

    public T above(int amount) { return inDirection(BlockFace.UP, amount); }
    public T above() { return inDirection(BlockFace.UP, 1); }

    public T below(int amount) { return inDirection(BlockFace.DOWN, amount); }
    public T below() { return inDirection(BlockFace.DOWN, 1); }

    public T inDirection(BlockFace face){ return inDirection(face, 1); }

    public T inDirection(BlockFace face, int amount){
        final int x = face.getModX() * amount;
        final int y = face.getModY() * amount;
        final int z = face.getModZ() * amount;

        return add(x, y, z);
    }

    public T subtract(ImmutableVector3i pos){ return subtract(pos.getX(), pos.getY(), pos.getZ()); }
    public T subtract(Location l){ return subtract(l.getBlockX(), l.getBlockY(), l.getBlockZ()); }
    public T subtract(Vector vector){ return subtract(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public T subtract(Vec3i vec3i){ return subtract(vec3i.getX(), vec3i.getY(), vec3i.getZ()); }

    public T add(ImmutableVector3i pos){ return add(pos.getX(), pos.getY(), pos.getZ()); }
    public T add(Location l){ return add(l.getBlockX(), l.getBlockY(), l.getBlockZ()); }
    public T add(Vector vector){ return add(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public T add(Vec3i vec3i){ return add(vec3i.getX(), vec3i.getY(), vec3i.getZ()); }

    public T add(int x, int y, int z){
        return set(
                getX() + x,
                getY() + y,
                getZ() + z
        );
    }

    public T subtract(int x, int y, int z){
        return set(
                getX() - x,
                getY() - y,
                getZ() - z
        );
    }

    public T divide(ImmutableVector3i vec) { return divide(vec.getX(), vec.getY(), vec.getZ()); }
    public T divide(int n) { return divide(n, n, n); }

    public T divide(int x, int y, int z) {
        return set(
                getX() / x,
                getY() / y,
                getZ() / z
        );
    }

    public T multiply(ImmutableVector3i vec) { return multiply(vec.getX(), vec.getY(), vec.getZ()); }
    public T multiply(int n) { return multiply(n, n, n); }

    public T multiply(int x, int y, int z) {
        return set(
                getX() * x,
                getY() * y,
                getZ() * z
        );
    }

    @Override
    public T clone() {
        return cloneAt(getX(), getY(), getZ());
    }

    public Vector toVec(){
        return new Vector(getX(), getY(), getZ());
    }

    public BlockVector2 to2D() {
        return BlockVector2.at(getX(), getZ());
    }

    public net.minecraft.core.BlockPos toNms(){
        return new net.minecraft.core.BlockPos(getX(), getY(), getZ());
    }

    public BlockVector3 toWE(){
        return BlockVector3.at(getX(), getY(), getZ());
    }

    public T zero() {
        return cloneAt(0, 0, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AbstractVector3i<?> i = (AbstractVector3i<?>) o;

        return new EqualsBuilder()
                .append(getX(), i.getX())
                .append(getY(), i.getY())
                .append(getZ(), i.getZ())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getX())
                .append(getY())
                .append(getZ())
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "x=" + x +
                ",y=" + y +
                ",z=" + z +
                '}';
    }
}
